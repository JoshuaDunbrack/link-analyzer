/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cleargovreferencefinder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
	 * For each client, a list of all of the pages in it to be analyzed.
	 * In the same order as the list of clients.
	 */
	ArrayList<HashSet<String>> pages = new ArrayList<>();

	/**
	 * Constructor that initializes both lists used in this program.
	 */
	public ClearGovReferenceFinder() {
		clients = getClientsFromSpreadsheet();
		System.out.println(clients.size());
		for (int i = 0; i < clients.size(); i++) {
			Client client = clients.get(i);
			pages.add(new HashSet<String>());
			pages.get(i).add(client.getUrlString());
		}
	}

	public static void main(String[] args) throws IOException {
		ClearGovReferenceFinder ref = new ClearGovReferenceFinder();
		Document doc = Jsoup.connect("https://www.southhadley.org/").get();
		for (String s : doc.select("a").eachAttr("href")) {
			if (s.toLowerCase().contains("cleargov") || s.toLowerCase().startsWith(
					"/")) {
				System.out.println(s);
			}
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

}
