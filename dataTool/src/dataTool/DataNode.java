package dataTool;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

/**
 * DataNode class that creates objects for the data we find and want to highlight.
 * 
 * @author Chris
 *
 */
public class DataNode {
	
	private String value;
	private int index;
	private int length;
	private String method;
	private String type;
	
	/**
	 * Constructor to build DataNode with an ASTNode, used for UpFinder mostly
	 * @param node= current SimpleName ASTNode selected
	 */
	public DataNode (ASTNode node) {
		if (node instanceof SimpleName) {
			value = ((SimpleName) node).getIdentifier();
			length = value.length();
			type = "variable";
		}
		else {
			value = ((SingleVariableDeclaration) node).getName().getIdentifier();
			length = node.getLength();
			type = "parameter";
		}
		index = node.getStartPosition();
	}
	
	/**
	 * Constructor to create DataNodes with just values, mainly for DownFinder
	 * @param val= Current variable name
	 * @param start= start position of the current variable
	 */
	public DataNode (String val, int start) {
		value = val;
		index = start;
		length = val.length();
	}
	
	/**
	 * Gets the value of the data
	 * @returns variable name
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * Gets the start position of current data in the source code
	 * @returns variable start position
	 */
	public int getStartPosition() {
		return index;
	}
	
	/**
	 * Gets the length of the data for highlighting
	 * @returns variable length
	 */
	public int getLength() {
		return length;
	}

}
