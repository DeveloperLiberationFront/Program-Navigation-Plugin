package dataTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jface.text.Position;

/**
 * Class that handles finding and storing where data is being declared in the source code.
 * Originally intended to extend OccurrencesFinder but that didn't work for the moment but
 * will might be needed for a DownFinder implementation.
 * 
 * @author Chris
 *
 */

public class UpFinder extends Finder {
	
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
	
//	/**
//	 * Function that checks to see if the current string is in the data list.
//	 * @param s: String to check
//	 * @return true if the string is a variable, else false
//	 */
//	@Override
//	public boolean contains(String s) {
//		return map.containsKey(s);
//	}
	
	/**
	 * This function returns a list of all the places where the current variable is 
	 * initialized which will determine where to highlight in the file.
	 * @param s: Current String
	 * @return ArrayList<ASTNode> of "up" occurrences for current variable name
	 */
	public ArrayList<DataNode> getUpOccurrences(String s, Position p) {
		ArrayList<DataNode> returnList = new ArrayList<DataNode>();
		for(Entry<String, ArrayList<DataNode>> entry : map.entrySet()) {
		    String key = entry.getKey();
		    ArrayList<DataNode> list = entry.getValue();
		    System.out.println(key + " " + s);
		    if( key.endsWith("." + s)) {
		    	System.out.println(key + " ||| " + p.offset);
		    	if( list.get(0).getStartPosition() == p.offset) {
		    		System.out.println("-------" + key + " " + s);
			    	returnList.addAll(entry.getValue());
		    	}
		    }

		    // do what you have to do here
		    // In your case, an other loop.
		}
		return returnList;	
	}
}
