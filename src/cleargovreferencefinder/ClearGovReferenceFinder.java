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
		branchAllWebsites(0);
		branchAllWebsites(1);
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
					"data/ClientList_WithWebsites_20DEC18.csv"));
			CSVParser records = CSVParser.parse(csvData,
												CSVFormat.EXCEL.withHeader());
			for (CSVRecord record : records) {
				Client client = new Client(record.get(0), record.get(1),
										   Integer.parseInt(record.get(2)),
										   record.get(3));
				clientList.add(client);
			}
		} catch (IOException e) {
			System.out.println(e);
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

		/**
		 *
		 *
		 *
		 * @return null
		 * @throws Exception if you messed up
		 */
		@Override
		protected Void call() throws Exception {
			int pageIndex = 0;
			while (pageIndex < clientSubpages.size()) {
				try {
					branchFromWebsite(urlString,
									  clientSubpages.get(pageIndex),
									  clientSubpages);
					System.out.printf("Analyzed %s\n", clientSubpages.get(
									  pageIndex));
				} catch (IOException e) {
				} finally {
					pageIndex += 1;
				}
			}
			return null;
		}

		private void branchFromWebsite(String baseURLString,
									   Webpage currentWebpage,
									   ArrayList<Webpage> listOfWebpages) throws IOException {
			String currentURLString = currentWebpage.getUrlString();
			int currentRecursionDepth = currentWebpage.getRecursionDepth();
			Document doc = Jsoup.connect(currentURLString).get();

			if (currentRecursionDepth < Webpage.MAX_RECURSION_DEPTH) {
				for (String tagString : doc.select("a").eachAttr("href")) {
					if (tagString.toLowerCase().contains(baseURLString) && !listOfWebpages.contains(
							new Webpage(tagString, currentRecursionDepth + 1))) {
						listOfWebpages.add(new Webpage(tagString,
													   currentRecursionDepth + 1));
					}
					else if (tagString.startsWith("/") && !listOfWebpages.contains(
							new Webpage(currentURLString + tagString,
										currentRecursionDepth + 1))) {
						listOfWebpages.add(new Webpage(
								currentURLString + tagString,
								currentRecursionDepth + 1));
					}
					else {
					}
				}
			}
		}

		/**
		 * The task for loading different subpages of a client concurrently.
		 */
		class SubpageTask extends Task<Void> {

			@Override
			protected Void call() throws Exception {
				return null;
			}
		}
	}

}
