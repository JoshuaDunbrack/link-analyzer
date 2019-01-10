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
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import javafx.concurrent.Task;
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
	public ClearGovReferenceFinder() {
		clients = getClientsFromSpreadsheet();
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
	private List<Client> getClientsFromSpreadsheet() {
		ArrayList<Client> clientList = new ArrayList<>();
		try {
			Reader csvData = new FileReader(new File(
					"data/Sample_File_ForJosh.csv"));
//					"data/ClientList_WithWebsites_20DEC18.csv"));
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

		public static final int MAX_THREADS = 1000;
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
			for (int i = 0; i < threads.length; i++) {
				threads[i] = new Thread();
				threads[i].start();
				while (threads[i].getState() != Thread.State.TERMINATED);
			}
		}

		protected Void call() throws Exception {
			int numPages = 0;
			do {
				Thread.sleep(5); //Because it doesn't need to be constantly checking.
				if (getFreeThreadIndex() != -1 && clientSubpages.size() > numPages) {
					SubpageTask task = new SubpageTask(numPages);
					Thread th = new Thread(task) {
						public void run() {
							try {
								task.call();
							} catch (Exception e) {
							};
						}
					};
					th.setDaemon(false);
					threads[getFreeThreadIndex()] = th;
					th.start();
					numPages += 1;
				}
			}
			while (!allThreadsFinished());
			System.out.println(client.getName() + " closed!");
			numClients -= 1;
			return null;
		}

		private int getFreeThreadIndex() {
			for (int i = 0; i < threads.length; i++) {
				if (threads[i].getState() == Thread.State.TERMINATED) {
					return i;
				}
			}
			return -1;
		}

		private boolean allThreadsFinished() {
			for (Thread t : threads) {
				if (t.getState() != Thread.State.TERMINATED) {
					return false;
				}
			}
			return true;
		}

		/**
		 * The task for loading different subpages of a client concurrently.
		 */
		class SubpageTask extends Task<Void> {

			int pageIndex;

			public SubpageTask(int pageIndex) {
				this.pageIndex = pageIndex;
			}

			@Override
			protected Void call() throws Exception {
				try {
					branchFromWebsite(urlString,
									  clientSubpages.get(pageIndex),
									  clientSubpages);
				} catch (Exception e) {
					//System.out.println(e);
				}
				return null;
			}

			private void branchFromWebsite(String baseURLString,
										   Webpage currentWebpage,
										   ArrayList<Webpage> listOfWebpages) throws IOException {
				String currentURLString = currentWebpage.getUrlString();
				if (currentURLString.endsWith(".pdf") || currentURLString.contains(
						".aspx")) {
					return;
				}
				Document doc = new Document("");
				try {
					int errorCount = 0;
					int prevErrorCount = 0;
					do {

						try {
							Connection connection = Jsoup.connect(
									currentURLString);
							doc = connection.get();
						} catch (SocketException e) {
							errorCount += 1;
						}
						if (errorCount > 100) {
							System.out.printf("Repeated errors for %s\n",
											  currentURLString);
							return;
						}
					}
					while (errorCount > prevErrorCount++);

					if (currentWebpage.getRecursionDepth() < Webpage.MAX_RECURSION_DEPTH) {
						analyzeDocument(baseURLString, currentWebpage,
										listOfWebpages, doc);
					}
				} catch (UnsupportedMimeTypeException | HttpStatusException | SocketTimeoutException | ConnectException | UnknownHostException e) {
				} catch (Exception e) {
					if (currentURLString.chars().filter(ch -> ch == '/').count() == 2 || true) {
						System.out.println(e);
						System.out.println(currentURLString);
					}
				}
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
						client.addLink(currentURLString, tagString,
									   OutgoingLinkType.LINK);
//						System.out.printf(
//								"%s link found at %s\n",
//								tagString, currentURLString);
					}
				}

				for (String tagString : doc.select("script").eachAttr("src")) {
					if (tagString.contains("cleargov.com")) {
						client.addLink(currentURLString, tagString,
									   OutgoingLinkType.WIDGET);
//						System.out.printf(
//								"%s link found at %s\n",
//								tagString, currentURLString);
					}
				}

				// TODO: superwidgets
			}
		}

	}

}
