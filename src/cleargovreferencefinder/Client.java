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

/**
 *
 * @author Joshua Dunbrack
 */
public class Client {

	private String organizationName;
	private String state;
	private int clearGovID;
	private String urlString;

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
		this.urlString = urlString;
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

	public String toString() {
		return "(" + getID() + ") " + getName() + ", " + getState() + ": " + urlString;
	}
}
