package dataTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import sun.print.resources.serviceui_de;

public class DownFinder extends Finder {
	private static Map<String, ArrayList<DataNode>> map; // contains name and first node for all data
	private static DownFinder instance; // current instance of UpFinder

	/**
	 * Singleton pattern because we only want one DownFinder
	 */
	private DownFinder() {
		map = new HashMap<String, ArrayList<DataNode>>();
	}
	
	/**
	 * Returns current DownFinder instance to keep track of data to the current point.
	 * @return DownFinder instance
	 */
	public static DownFinder getInstance() {
		if (instance == null) {
			instance = new DownFinder();
		}
		return instance;
	}
	
	/**
	 * Function to add a variable name and it's down occurrences to the map.
	 * @param s: String name of the variable
	 * @param node: ASTNode containing the variable declaration
	 */
	public static void add(String key, int start, String type, String method) {
		ArrayList<DataNode> list;
		DataNode dn = new DataNode(key, start, type, method);
		if (!map.containsKey(key)) {
			list = new ArrayList<DataNode>();
			list.add(dn);
			map.put(key, list);
		}
		else {
			list = map.get(key);
			list.add(dn);
			map.put(key, list);
		}
	}
	
	public static void add(ASTNode node, String type) {
		ArrayList<DataNode> list;
		DataNode dn = new DataNode(node, type);
		String key = dn.getValue();
		if (!map.containsKey(key)) {
			list = new ArrayList<DataNode>();
			list.add(dn);
			map.put(key, list);
		}
		else {
			list = map.get(key);
			list.add(dn);
			map.put(key, list);
		}
	}
	
	/**
	 * Function that checks to see if the current string is in the data list.
	 * @param s: String to check
	 * @returns true if the string is a variable, else false
	 */
	public boolean contains(String s) {
		return map.containsKey(s);
	}
	
	/**
	 * This function returns a list of all the places where the current variable is 
	 * used.
	 * @param s: Current String
	 * @returns ArrayList<ASTNode> of "down" occurrences for current variable name
	 */
	public ArrayList<DataNode> getDownOccurrences(String s) {
		return map.get(s);
	}
	
	/**
	 * Function to check if selected text is actually a variable.
	 * @param var
	 * @param index
	 * @param sourceCode
	 * @returns true if text is use of a variable, else false
	 */
	public boolean variableCheck(String var, int index, String sourceCode) {
		boolean check = false;
		if(sourceCode.substring(index+var.length(),index+var.length()+1).matches("[a-zA-Z0-9]") || sourceCode.substring(index-1,index).matches("[a-zA-Z0-9]")) {
			return false;
		}
		if(isComment(var, index, sourceCode)) {
			return false;
		}
		return true;
	}
	
	/**
	 * Checks to see if the visited variable name is inside a comment so we don't highlight it.
	 * Kind of a hack
	 * @param var: String name of the data
	 * @param index: int start position of the data
	 * @param sourceCode: String of the entire code
	 * @returns true if name is within a comment, else false
	 */
	private boolean isComment(String var, int index, String sourceCode) {
		//Check if line starts with //, /*, /**, or *
		String temp = sourceCode.substring(0, index);
		if(temp.lastIndexOf("/**") > temp.lastIndexOf("*/") || temp.lastIndexOf("/*") > temp.lastIndexOf("*/")
				|| temp.lastIndexOf("//") > temp.lastIndexOf("\n")) {
			return true;
		}
		return false;
	}
}