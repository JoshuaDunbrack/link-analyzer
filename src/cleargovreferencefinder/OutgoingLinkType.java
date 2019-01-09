/* *****************************************
 * ClearGov Website Analyzer
 *
 * Author: Josh Dunbrack
 * Date: Jan 9, 2019
 * Time: 4:22:25 PM
 *
 * Project: ClearGovReferenceFinder
 * Package: cleargovreferencefinder
 * File: OutgoingLinkType
 * Description:
 *
 * ****************************************
 */
package cleargovreferencefinder;

/**
 *
 * @author Joshua Dunbrack
 */
public enum OutgoingLinkType {
	LINK("Link"), IMAGE("Image"), WIDGET("Widget"), SUPERWIDGET("Superwidget");
	private String typeString;

	OutgoingLinkType(String typeString) {
		this.typeString = typeString;
	}

	public String toString() {
		return typeString;
	}
}
