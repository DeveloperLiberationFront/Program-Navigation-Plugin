package dataTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
	public static void add(String key, int start) {
		ArrayList<DataNode> list;
		DataNode dn = new DataNode(key, start);
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
	 * Searches the class code and finds instances of data going "down" to fill map.
	 * @param list: Set of all data variable names
	 * @param code: String of source code
	 */
	public void searchClassDown(HashSet<String> list, String code) {
		for(String var: list) {
			int index = code.indexOf(var);
			while(index >= 0) {
				if(variableCheck(var, index, code)) {
					add(var, index);
				}
				index = code.indexOf(var, index+1);
			}
		}
	}
	
	/**
	 * Function to check if selected text is actually a variable.
	 * @param var
	 * @param index
	 * @param sourceCode
	 * @returns true if text is use of a variable, else false
	 */
	private boolean variableCheck(String var, int index, String sourceCode) {
		boolean check = false;
		if(sourceCode.substring(index+var.length(),index+var.length()+1).matches("[a-zA-Z0-9]") || sourceCode.substring(index-1,index).matches("[a-zA-Z0-9]")) {
			return false;
		}
		if(isUp(var,index)) { 
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

	/**
	 * Function that checks to see if the variable is not a declaration/initialization
	 * @param var: String variable name
	 * @param start: int start position
	 * @returns true if variable is not "up", else false
	 */
	private boolean isUp(String var, int start) {
		UpFinder up = UpFinder.getInstance();
		ArrayList<DataNode> varList = up.getUpOccurrences(var);
		for(DataNode node: varList) {
			if(node.getStartPosition() <= start && node.getStartPosition()+node.getLength()+1 >= start) {
				return true;
			}
		}
		return false;
	}

	public static void searchProjectDown() {
		//TODO
		/*
		 * http://stackoverflow.com/questions/13980726/using-search-engine-to-implement-call-hierarchy-getting-all-the-methods-that-in
		 */
	}
}
