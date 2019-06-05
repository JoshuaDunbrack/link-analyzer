/* *****************************************
 * ClearGov Website Analyzer
 *
 * Author: Josh Dunbrack
 * Date: Jan 8, 2019
 * Time: 10:33:23 AM
 *
 * Project: ClearGovReferenceFinder
 * Package: cleargovreferencefinder
 * File: Webpage
 * Description: A data structure that holds the information about each subpage URL, including recursion depth.
 *
 * ****************************************
 */
package cleargovreferencefinder;

import java.util.Objects;

/**
 *
 * @author Joshua Dunbrack
 */
public class Webpage {

	public static final int MAX_RECURSION_DEPTH = 5;
	private String urlString;
	private int recursionDepth;

	public Webpage(String urlString, int recursionDepth) {
		this.urlString = urlString.replace("http://", "https://");
		if (this.urlString.endsWith("/")) {
			this.urlString = this.urlString.substring(0,
													  this.urlString.length() - 1);
		}
		this.recursionDepth = recursionDepth;
	}

	public String getUrlString() {
		return urlString;
	}

	public int getRecursionDepth() {
		return recursionDepth;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Webpage other = (Webpage) obj;
		if (!Objects.equals(this.urlString, other.getUrlString())) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		return String.format("(%s, %d)", urlString, recursionDepth);
	}

	public void makeHTTP() {
		this.urlString = this.urlString.replace("http://", "https://");
	}
}
