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
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jsoup.Jsoup;
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

	/**
	 * Constructor that initializes both lists used in this program.
	 */
	public ClearGovReferenceFinder() {
		clients = getClientsFromSpreadsheet();
		for (int i = 0; i < clients.size(); i++) {
			branchAllWebsites(i);
		}
	}

	/**
	 * Fills out the specified index of the pages variable.
	 *
	 * @param clientIndex The index of the client being analyzed
	 */
	public void branchAllWebsites(int clientIndex) {
		ClientTask task = new ClientTask(clients.get(clientIndex));
		Thread th = new Thread(task) {
			public void run() {
				try {
					task.call();
				} catch (Exception e) {
				};
			}
		};
		th.setDaemon(true);
		th.start();
		try {
			Thread.sleep(100);
		} catch (InterruptedException ex) {
			Logger.getLogger(ClearGovReferenceFinder.class.getName()).log(
					Level.SEVERE,
					null,
					ex);
		}
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

		Client client;
		ArrayList<Webpage> clientSubpages;
		String urlString;

		/**
		 * Constructs the task with the model and the number of iterations to
		 * run through
		 */
		public ClientTask(Client client) {
			this.client = client;
			this.urlString = client.getUrlString();
			this.clientSubpages = client.getSubpages();
		}

		protected Void call() throws Exception {
			int numPages = 0;
			while (true) {
				if (clientSubpages.size() > numPages) {
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
					th.start();
					numPages += 1;
				}
				Thread.sleep(5);
			}
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
				int currentRecursionDepth = currentWebpage.getRecursionDepth();
				if (currentURLString.chars().filter(ch -> ch == '.').count() > 2) {
					return;
				}
				Document doc = Jsoup.connect(currentURLString).get();
				if (currentRecursionDepth < Webpage.MAX_RECURSION_DEPTH) {
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
						else if (tagString.contains("cleargov")) {
							System.out.printf(
									"%s link found at %s\n",
									tagString, currentURLString);
						}
					}

					for (String tagString : doc.select("script").eachAttr("src")) {
						if (tagString.contains("cleargov")) {
							System.out.printf(
									"%s link found at %s\n",
									tagString, currentURLString);
						}
					}
					//System.out.println(currentWebpage.toString());
				}
			}
		}

	}

}
