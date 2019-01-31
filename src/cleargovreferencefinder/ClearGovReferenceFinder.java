/* *****************************************
 * ClearGov Website Analyzer
 *
 * Author: Josh Dunbrack
 * Date: Jan 7, 2019
 * Time: 11:37:48 AM
 *
 * Project: ClearGovReferenceFinder
 * Package: cleargovreferencefinder
 * File: ClearGovReferenceFinder
 *
 * Description: A data structure to hold information about analyzed clients,
 *              including name, ID, state, and website URL.
 * ****************************************
 */
package cleargovreferencefinder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import javafx.concurrent.Task;
import javax.net.ssl.SSLException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;

/**
 *
 * @author Joshua Dunbrack
 */
public class ClearGovReferenceFinder {

	/**
	 * A list of all of the websites that this program is set to examine.
	 * Read in from input file.
	 */
	List<Client> clients = new ArrayList<>();

	int numClients;

	/**
	 * Constructor that initializes both lists used in this program.
	 */
	public ClearGovReferenceFinder(String fileNameString) {
		clients = getClientsFromSpreadsheet(fileNameString);
		numClients = clients.size();
		for (int i = 0; i < clients.size(); i++) {
			handleClient(clients.get(i));
		}
		do {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
			}
		}
		while (numClients > 0);
		try {
			FileWriter writer = new FileWriter("output.csv");
			for (Client c : clients) {
				writer.write(c.toString());
			}
			writer.close();
		} catch (IOException e) {
		}
	}

	private void handleClient(Client client) {
		ClientTask task = new ClientTask(client);
		Thread th = new Thread(task) {
			public void run() {
				try {
					task.call();
				} catch (Exception e) {
				};
			}
		};
		th.setDaemon(false);
		th.start();
	}

	/**
	 * Generates and returns the list of clients from the provided spreadsheet.
	 *
	 * @return The list of clients generated from the spreadsheet.
	 */
	private List<Client> getClientsFromSpreadsheet(String fileNameString) {
		ArrayList<Client> clientList = new ArrayList<>();
		try {
			Reader csvData = new FileReader(new File(fileNameString));
			CSVParser records = CSVParser.parse(csvData,
												CSVFormat.EXCEL.withHeader());
			for (CSVRecord record : records) {
				Client client = new Client(record.get(0), record.get(1),
										   Integer.parseInt(record.get(2)),
										   record.get(3));
				clientList.add(client);
			}
			System.out.println(clientList.size());
		} catch (IOException e) {
			System.out.println(e);
		} catch (Exception e) {
			System.out.printf("Uh-oh! %s\n", e);
		} finally {
			return clientList;
		}
	}

	/**
	 * The task for loading clients concurrently.
	 */
	class ClientTask extends Task<Void> {

		public static final int MAX_THREADS = 2000;
		Client client;
		ArrayList<Webpage> clientSubpages;
		String urlString;
		Thread[] threads = new Thread[MAX_THREADS];

		/**
		 * Constructs the task with the model and the number of
		 * iterations to
		 * run through
		 */
		public ClientTask(Client client) {
			this.client = client;
			this.urlString = client.getUrlString();
			this.clientSubpages = client.getSubpages();
//			for (int i = 0; i < threads.length; i++) {
//				threads[i] = new Thread();
//				threads[i].start();
//				while (threads[i].getState() != Thread.State.TERMINATED);
//			}
		}

		protected Void call() throws Exception {
			int numPages = 0;
			int runs = 0;
			do {
				runs += 1;
				if (runs % 10000 == 0) {
					System.out.println("Still running: " + client.getName());
					printActiveThreads();
					System.out.println(runs);
				}
				if (runs > 120000) {
					printActiveThreads();
					System.out.println("Cancelling " + client.getName());
					break;
				}
				if (getFreeThreadIndex() != -1 && clientSubpages.size() > numPages && !client.isFailed()) {
					SubpageTask task = new SubpageTask(numPages, client);
					Thread th = new Thread(task) {
						public void run() {
							try {
								task.call();
							} catch (Exception e) {
							};
						}
					};
					th.setDaemon(true);
					threads[getFreeThreadIndex()] = th;
					th.start();
					numPages += 1;
				}
				Thread.sleep(5); //Because it doesn't need to be constantly checking.
			}

			while (!allThreadsFinished() || (runs < 500 && !client.isFailed()));
			System.out.println(client.getName() + " closed!");
			numClients -= 1;
			if (client.isFailed()) {
				System.out.println("FAIL");
			}

			else {
				System.out.println("SUCC");
			}

			System.out.println(numClients);

			return null;
		}

		private int getFreeThreadIndex() {
			for (int i = 0; i < threads.length; i++) {
				if (null == threads[i] || threads[i].getState() == Thread.State.TERMINATED) {
					return i;
				}
			}
			return -1;
		}

		private boolean allThreadsFinished() {
			for (Thread t : threads) {
				if (null != t && t.getState() != Thread.State.TERMINATED) {
					return false;
				}
			}
			return true;
		}

		private void printActiveThreads() {
			int timed = 0;
			int run = 0;
			int other = 0;
			for (Thread t : threads) {
				if (null == t) {
					continue;
				}
				if (t.getState() == Thread.State.RUNNABLE) {
					run++;
				}
				else if (t.getState() == Thread.State.TIMED_WAITING) {
					timed++;
				}
				else if (t.getState() != Thread.State.TERMINATED) {
					other++;
				}
			}
			System.out.println("TIMED: " + timed);
			System.out.println("  RUN: " + run);
			System.out.println("OTHER: " + other);
		}

		/**
		 * The task for loading different subpages of a client concurrently.
		 */
		class SubpageTask extends Task<Void> {

			int pageIndex;
			Client client;

			public SubpageTask(int pageIndex, Client client) {
				this.pageIndex = pageIndex;
				this.client = client;
			}

			@Override
			protected Void call() throws Exception {
				try {
					Exception e = branchFromWebsite(urlString,
													clientSubpages.get(pageIndex),
													clientSubpages);
					if (null != e) { //success
						client.fail(e);
					}
				} catch (Exception e) {
					//System.out.println(e);
				}
				return null;
			}

			private Exception branchFromWebsite(String baseURLString,
												Webpage currentWebpage,
												ArrayList<Webpage> listOfWebpages) throws IOException {
				if (client.isFailed()) {
					return null;
				}
				String currentURLString = currentWebpage.getUrlString();
				if (currentURLString.endsWith(".pdf") || currentURLString.endsWith(
						".doc") || currentURLString.contains(".aspx") || currentURLString.contains(
						"twitter.com") || currentURLString.contains("search") || currentURLString.contains(
						"login") || currentURLString.contains("HDAACORG")) {
					return null;
				}
				Document doc = null;
				try {
					int errorCount = 0;
					int prevErrorCount = 0;
					do {
						//System.out.println(currentURLString);
						try {
							Connection connection = Jsoup.connect(
									currentURLString);
							doc = connection.get();
						} catch (SocketException e) {
							errorCount += 1;
							try {
								Thread.sleep(1000);
							} catch (InterruptedException ex) {

							}
							if (errorCount > 20) {
								System.out.printf(
										"Repeated socket errors for %s\n",
										currentURLString);

								return e;
							}
						} catch (HttpStatusException e) {
							try {
								Thread.sleep(30000);
							} catch (InterruptedException ex) {
							}
							errorCount += 1;
							if (errorCount > 5) {
								return null;
							}
						}
					}
					while (errorCount > prevErrorCount++);
					if (currentWebpage.getRecursionDepth() < Webpage.MAX_RECURSION_DEPTH) {
						analyzeDocument(baseURLString, currentWebpage,
										listOfWebpages, doc);
					}
				} catch (UnsupportedMimeTypeException | HttpStatusException | SocketTimeoutException | ConnectException | UnknownHostException | MalformedURLException e) {
					return null;
				} catch (SSLException e) {
					return e;
				} catch (IOException e) {
					if (!e.toString().contains("zero bytes")) {
						System.out.println("IO: " + e);
					}
					// return e;
				} catch (Exception e) {
					System.out.println("UN: " + e);
					// return e;
					//System.out.println(currentWebpage.getUrlString());
				}
				return null;
			}

			private void analyzeDocument(String baseURLString,
										 Webpage currentWebpage,
										 ArrayList<Webpage> listOfWebpages,
										 Document doc) {
				String currentURLString = currentWebpage.getUrlString();
				int currentRecursionDepth = currentWebpage.getRecursionDepth();
				for (String tagString : doc.select("a").eachAttr("href")) {
					if (tagString.toLowerCase().contains(baseURLString) && !listOfWebpages.contains(
							new Webpage(tagString, currentRecursionDepth + 1))) {
						listOfWebpages.add(new Webpage(tagString,
													   currentRecursionDepth + 1));
					}
					else if (tagString.startsWith("/") && !listOfWebpages.contains(
							new Webpage(baseURLString + tagString,
										currentRecursionDepth + 1))) {
						listOfWebpages.add(new Webpage(
								baseURLString + tagString,
								currentRecursionDepth + 1));
					}
					else if (tagString.contains("cleargov.com")) {
						client.addLink(baseURLString, currentURLString,
									   tagString,
									   OutgoingLinkType.LINK);
//						System.out.printf(
//								"%s link found at %s\n",
//								tagString, currentURLString);
					}
				}

				for (String tagString : doc.select("script").eachAttr("src")) {
					if (tagString.contains("cleargov.com")) {
						client.addLink(baseURLString, currentURLString,
									   tagString,
									   OutgoingLinkType.WIDGET);
//						System.out.printf(
//								"%s link found at %s\n",
//								tagString, currentURLString);
					}
				}

				for (String tagString : doc.select("iframe").eachAttr("src")) {
					if (tagString.contains("superwidget")) {
						System.out.println("Superwidget!");
						client.addLink(baseURLString, currentURLString,
									   tagString,
									   OutgoingLinkType.SUPERWIDGET);
//						System.out.printf(
//								"%s link found at %s\n",
//								tagString, currentURLString);
					}
				}
			}
		}

	}

}
