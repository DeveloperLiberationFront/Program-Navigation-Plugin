package dataTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

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
import org.eclipse.jface.text.Position;

import sun.print.resources.serviceui_de;

public class DownFinder extends Finder {	
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
	
//	/**
//	 * Function that checks to see if the current string is in the data list.
//	 * @param s: String to check
//	 * @returns true if the string is a variable, else false
//	 */
//	@Override
//	public boolean contains(String s) {
//		return map.containsKey(s);
//	}
	
	/**
	 * This function returns a list of all the places where the current variable is 
	 * used.
	 * @param s: Current String
	 * @returns ArrayList<ASTNode> of "down" occurrences for current variable name
	 */
	public ArrayList<DataNode> getDownOccurrences(String s, Position p) {
		ArrayList<DataNode> returnList = new ArrayList<DataNode>();
		for(Entry<String, ArrayList<DataNode>> entry : map.entrySet()) {
		    String key = entry.getKey();
		    System.out.println(key + " " + s);
		    if( key.endsWith("." + s)) {
		    	System.out.println("-------" + key + " " + s);
		    	returnList.addAll(entry.getValue());
		    }

		    // do what you have to do here
		    // In your case, an other loop.
		}
		return returnList;	
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
