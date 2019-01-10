/* *****************************************
 * ClearGov Website Analyzer
 *
 * Author: Josh Dunbrack
 * Date: Jan 9, 2019
 * Time: 4:10:23 PM
 *
 * Project: ClearGovReferenceFinder
 * Package: cleargovreferencefinder
 * File: OutgoingLink
 * Description:
 *
 * ****************************************
 */
package cleargovreferencefinder;

/**
 *
 * @author Joshua Dunbrack
 */
public class OutgoingLink {

	String sourceURLString;
	String destinationURLString;
	OutgoingLinkType type;

	public OutgoingLink(String sourceURLString, String destinationURLString,
						OutgoingLinkType type) {
		this.sourceURLString = sourceURLString;
		this.destinationURLString = destinationURLString;
		this.type = type;
	}

	public String getSourceURLString() {
		return sourceURLString;
	}

	public String getDestinationURLString() {
		return destinationURLString;
	}

	public OutgoingLinkType getType() {
		return type;
	}

	public String toString() {
		return String.join("\",\"", sourceURLString, destinationURLString,
						   type.toString());
	}
}
