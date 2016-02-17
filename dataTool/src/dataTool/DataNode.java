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
	
	private String binding;
	private String value;
	private int index;
	private int length;
	private SimpleName sn;
	private SimpleName method;
	private String type;
	private String signature;
	private Method parameterMethod;
	private boolean isHighlighted;

//	/**
//	 * Constructor to create DataNodes with just values, mainly for DownFinder
//	 * @param val= Current variable name
//	 * @param start= start position of the current variable
//	 */
//	public DataNode (String val, int start, String nodeType, Method call) {
//		
//		
//		value = val;
//		index = start;
//		length = val.length();
//		type = nodeType;
//		method = call;
//		if( method != null ) {
//			signature = method.getSignature() + "." + value;
//		} else {
//			signature = "null";
//		}
//	}
	
	public DataNode( SimpleName sn, SimpleName method ) {
		this.sn = sn;
		this.method = method;
		value = sn.getFullyQualifiedName();
		index = sn.getStartPosition();
		length = value.length();
		isHighlighted = false;
		this.binding = sn.resolveBinding().toString();
	}
	public void setStartPosition( int i ) {
		index = i;
	}
	
	public void highLight() {
//		isHighlighted = true;
	}
	
	public boolean isHighlighted() {
		return isHighlighted;
	}
	/**
	 * Gets the value of the data
	 * @returns variable name
	 */
	public String getValue() {
		return this.value;
	}
	
	public String getBinding() {
		return binding;
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
	public SimpleName getMethod() {
		return this.method;
	}
	
	public String getSignature() {
		return this.signature;
	}
//	/**
//	 * Checks to see if current node is a parameter, only want to display box when actual
//	 * param is selected
//	 * @returns true if node is a parameter, else false
//	 */
//	public boolean isParameterSelected(int pos) {
//		if(pos < index || pos > index+length) {
//			return false;
//		}
//		return (type.equals(PARAM_UP) || (type.equals(PARAM_DOWN)));
//	}
	
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
	
	
	public void setParameterMethod(Method m) {
		this.parameterMethod = m;
	}
	
	public Method getParameterMethod() {
		return this.parameterMethod;
	}

}
