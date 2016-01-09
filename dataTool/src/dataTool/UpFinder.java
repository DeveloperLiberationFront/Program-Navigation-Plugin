package dataTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NodeFinder;

/**
 * /**
 * Class that handles finding and storing where data is being declared in the source code.
 * Originally intended to extend OccurrencesFinder but that didn't work for the moment but
 * will might be needed for a DownFinder implementation.
 * 
 * @author Chris
 *
 */

public class UpFinder {
	
	private Map<String, ArrayList<ASTNode>> map;
	private static UpFinder instance;

	/**
	 * Singleton pattern because we only want one DeclarationFinder
	 */
	private UpFinder() {
		map = new HashMap<String, ArrayList<ASTNode>>();
	}
	
	/**
	 * Returns current DeclarationFinder instance to keep track of data.
	 * @return DeclarationFinder instance
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
	public void add(String s, ASTNode node) {
		ArrayList<ASTNode> list;
		if (!map.containsKey(s)) {
			list = new ArrayList<ASTNode>();
			list.add(node);
			map.put(s, list);
		}
		else {
			list = map.get(s);
			list.add(node);
			map.put(s, list);
		}
	}
	
	/**
	 * Function that checks to see if the current string is in the data list.
	 * @param s: String to check
	 * @return true if the string is a variable, else false
	 */
	public boolean containsVar(String s) {
		return map.containsKey(s);
	}
	
	/**
	 * This function returns a list of all the places where the current variable is 
	 * initialized which will determine where to highlight in the file.
	 * @param s: Current String
	 * @return ArrayList<ASTNode> of "up" occurrences for current variable name
	 */
	public ArrayList<ASTNode> getUpOccurences(String s) {
		return map.get(s);
	}
}
