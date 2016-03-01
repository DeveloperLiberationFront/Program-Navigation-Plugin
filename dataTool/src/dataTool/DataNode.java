package dataTool;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jface.text.Position;

import jdk.internal.org.objectweb.asm.tree.analysis.Value;

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
	
	private String key;
	private String name;
	private int startPosition;
	private int length;
	private int parameterIndex;
	private SimpleName sn;
	private String type;
	private Method declarationMethod;
	private Method invocationMethod;
	private Position position;

	public DataNode( SimpleName sn ) {
		this.sn = sn;
		name = sn.getFullyQualifiedName();
		startPosition = sn.getStartPosition();
		length = name.length();
		position = new Position( startPosition, length );
		parameterIndex = -1;
		this.key = sn.resolveBinding().getKey();
	}
	public void setStartPosition( int i ) {
		startPosition = i;
	}
	
	/**
	 * Gets the value of the data
	 * @returns variable name
	 */
	public String getValue() {
		return this.name;
	}
	
	/**
	 * Gets the start position of current data in the source code
	 * @returns variable start position
	 */
	public int getStartPosition() {
		return this.startPosition;
	}
	
	/**
	 * Gets the length of the data for highlighting
	 * @returns int variable length
	 */
	public int getLength() {
		return this.length;
	}
	public int getParameterIndex() {
		return parameterIndex;
	}
	public void setParameterIndex( int p ) {
		parameterIndex = p;
	}
	public Method getDeclarationMethod() {
		return declarationMethod;
	}
	public void setDeclarationMethod( Method m ) {
		declarationMethod = m;
	}
	public Method getInvocationMethod() {
		return invocationMethod;
	}
	
	public void setInvocationMethod( Method m ) {
		invocationMethod = m;
	}
	/**
	 * Checks to see if current node is a parameter, only want to display box when actual
	 * param is selected
	 * @returns true if node is a parameter, else false
	 */
	public boolean isParameterSelected(int pos) {
		if(pos < startPosition || pos > startPosition+length) {
			return false;
		}
		return true;
	}
	public String getKey() {
		return key;
	}
	@Override
	public int compareTo(Object o) {
		if( o == null || !( o instanceof DataNode ) ) {
			return 1;
		}
		return startPosition - ((DataNode)o).getStartPosition();
	}
	@Override
	public String toString() {
		return key;
	}
	public Position getPosition() {
		return position;
	}

}