package dataTool;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;

/**
 * DataNode class that creates objects for the data we find and want to highlight.
 * 
 * @author Chris
 *
 */
public class DataNode implements Comparable {
	
	final public static String PARAM_UP = "parameterUp";
	final public static String PARAM_DOWN = "parameterDown";
	final public static String VAR_DECL = "variableDecl";
	final public static String CLASS_VAR = "variableClass";
	final public static String FOR_VAR = "variableFor";
	final public static String VAR = "variable";
	
	private String value;
	private int index;
	private int length;
	private Method method;
	private String type;
	private String signature;
//	
//	/**
//	 * Constructor to build DataNode with an ASTNode, used for UpFinder mostly
//	 * @param node: current SimpleName ASTNode selected
//	 */
//	public DataNode (ASTNode node, String nodeType) {
//		if (node instanceof SimpleName) {
//			value = ((SimpleName) node).getIdentifier();
//			length = value.length();
//			type = nodeType;
//			method = null;
//		}
//		else {
//			value = ((SingleVariableDeclaration) node).getName().getIdentifier();
//			length = node.getLength();
//			type = nodeType;
//			if(node.getParent() instanceof MethodDeclaration) {
//				method = ((MethodDeclaration) node.getParent()).getName().getIdentifier();
//			}
//		}
//		index = node.getStartPosition();
//	}
	
	/**
	 * Constructor to create DataNodes with just values, mainly for DownFinder
	 * @param val= Current variable name
	 * @param start= start position of the current variable
	 */
	public DataNode (String val, int start, String nodeType, Method call) {
		value = val;
		index = start;
		length = val.length();
		type = nodeType;
		method = call;
		if( method != null ) {
			signature = method.getSignature() + "." + value;
		} else {
			signature = "null";
		}
	}
	
	/**
	 * Gets the value of the data
	 * @returns variable name
	 */
	public String getValue() {
		return this.value;
	}
	
	/**
	 * Gets the start position of current data in the source code
	 * @returns variable start position
	 */
	public int getStartPosition() {
		return this.index;
	}
	
	/**
	 * Gets the length of the data for highlighting
	 * @returns int variable length
	 */
	public int getLength() {
		return this.length;
	}
	
	/**
	 * Gets the type of the DataNode
	 * @returns String of data type
	 */
	public String getType() {
		return this.type;
	}
	
	/**
	 * Gets the name of the method of the node if one exists
	 * @returns string method
	 */
	public Method getMethod() {
		return this.method;
	}
	
	public String getSignature() {
		return this.signature;
	}
	/**
	 * Checks to see if current node is a parameter, only want to display box when actual
	 * param is selected
	 * @returns true if node is a parameter, else false
	 */
	public boolean isParameterSelected(int pos) {
		if(pos < index || pos > index+length) {
			return false;
		}
		return (type.equals(PARAM_UP) || (type.equals(PARAM_DOWN)));
	}
	
	public boolean isParameter() {
		return type.equals(PARAM_UP) || type.equals(PARAM_DOWN);
	}

	@Override
	public int compareTo(Object o) {
		if( o == null || !( o instanceof DataNode ) ) {
			return 1;
		}
		return index - ( ( DataNode ) o ).getStartPosition();
	}
	
	public String getMethodSignature() {
		if( method != null ) {
			return method.getSignature();
		}
		return "null";
	}

}
