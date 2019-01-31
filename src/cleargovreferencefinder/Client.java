/* *****************************************
 * ClearGov Website Analyzer
 *
 * Author: Josh Dunbrack
 * Date: Jan 7, 2019
 * Time: 11:51:42 AM
 *
 * Project: ClearGovReferenceFinder
 * Package: cleargovreferencefinder
 * File: Client
 *
 * Description: A data structure to hold information about analyzed clients,
 *              including name, ID, state, and website URL.
 * ****************************************
 */
package cleargovreferencefinder;

import java.util.ArrayList;

/**
 *
 * @author Joshua Dunbrack
 */
public class Client {

	private String organizationName;
	private String state;
	private int clearGovID;
	private String urlString;
	private ArrayList<Webpage> subpages;
	private ArrayList<OutgoingLink> outgoingLinks = new ArrayList<>();
	private boolean fail = false;
	private Exception e = null;

	/**
	 * Constructs the Client object with the provided parameters.
	 *
	 * @param organizationName The name of the organization for the website
	 * @param state            The state in which the organization is based
	 * @param clearGovID       The ClearGov-ID of the given organization
	 * @param urlString        The provided website from which the spider will
	 *                         spread
	 */
	public Client(String organizationName, String state, int clearGovID,
				  String urlString) {
		this.organizationName = organizationName;
		this.state = state;
		this.clearGovID = clearGovID;
		this.urlString = urlString.replace("http://", "https://");
		subpages = new ArrayList<>();
		subpages.add(new Webpage(urlString, 0));
	}

	public String getName() {
		return organizationName;
	}

	public String getState() {
		return state;
	}

	public int getID() {
		return clearGovID;
	}

	public String getUrlString() {
		return urlString;
	}

	public void fail(Exception e) {
		if (!fail) {
			this.fail = true;
			this.e = e;
		}
	}

	public boolean isFailed() {
		return fail;
	}

	@Override
	public String toString() {
		if (fail) {
			return "\"" + String.join("\",\"", getName(), getState(),
									  String.valueOf(getID()),
									  getUrlString(), "ERROR", e.toString(),
									  String.valueOf(getSubpages().size())) + "\"\n";
		}
		if (outgoingLinks.size() == 0) {
			return "\"" + String.join("\",\"", getName(), getState(),
									  String.valueOf(getID()),
									  getUrlString(), "NONE", "NONE",
									  String.valueOf(getSubpages().size())) + "\"\n";
		}
		else {
			String retStr = "";
			for (OutgoingLink link : outgoingLinks) {
				retStr += "\"" + String.join("\",\"", getName(), getState(),
											 String.valueOf(getID()),
											 link.toString(), String.valueOf(
											 getSubpages().size())) + "\"\n";

			}
			return retStr;
		}
	}

	public ArrayList<Webpage> getSubpages() {
		return subpages;
	}

	public ArrayList<OutgoingLink> getOutgoingLinks() {
		return outgoingLinks;
	}

	public void addLink(String baseURLString, String sourceURLString,
						String destinationURLString,
						OutgoingLinkType type) {
		outgoingLinks.add(
				new OutgoingLink(baseURLString, sourceURLString,
								 destinationURLString, type));
	}
}
