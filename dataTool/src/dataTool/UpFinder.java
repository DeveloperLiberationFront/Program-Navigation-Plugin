package dataTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NodeFinder;

/**
 * Class that handles finding and storing where data is being declared in the source code.
 * Originally intended to extend OccurrencesFinder but that didn't work for the moment but
 * will might be needed for a DownFinder implementation.
 * 
 * @author Chris
 *
 */

public class UpFinder extends Finder {
	
	private Map<String, ArrayList<DataNode>> map; // contains name and first node for all data
	private static UpFinder instance; // current instance of UpFinder

	/**
	 * Singleton pattern because we only want one DeclarationFinder
	 */
	private UpFinder() {
		map = new HashMap<String, ArrayList<DataNode>>();
	}
	
	/**
	 * Returns current UpFinder instance to keep track of data to the current point.
	 * @return UpFinder instance
	 */
	public static UpFinder getInstance() {
		if (instance == null) {
			instance = new UpFinder();
		}
		return instance;
	}
	
	/**
	 * Function to add a variable name and it's "up" occurrences to the DeclarationFinder map.
	 * @param s: String name of the variable
	 * @param node: ASTNode containing the variable declaration
	 */
	public void add(String key, ASTNode node) {
		ArrayList<DataNode> list;
		DataNode dn = new DataNode(node);
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
	 * @return true if the string is a variable, else false
	 */
	public boolean contains(String s) {
		return map.containsKey(s);
	}
	
	/**
	 * This function returns a list of all the places where the current variable is 
	 * initialized which will determine where to highlight in the file.
	 * @param s: Current String
	 * @return ArrayList<ASTNode> of "up" occurrences for current variable name
	 */
	public ArrayList<DataNode> getUpOccurrences(String s) {
		return map.get(s);
	}
	
	public void searchClassUp(String code) {
		//Mostly implemented in Visitor class parseData
		
	}
	
	public void searchProjectUp() {
		//TODO
		/*
		 * http://stackoverflow.com/questions/13980726/using-search-engine-to-implement-call-hierarchy-getting-all-the-methods-that-in
		 */
		
	}
}
