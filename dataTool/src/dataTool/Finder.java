package dataTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

import dataTool.annotations.SuggestedSelectionAnnotation;

public class Finder {
	protected static Map<String, TreeSet<DataNode>> map; // contains name and first node for all data
	protected static Map<String, Map> param_map;
	final public static String UP = "up";
	final public static String DOWN = "down";
	
	private static Finder currentFinder;
	private String goToName = null;
	private int goToOffset = -1;
	
	private Finder () {
		map = new HashMap<String, TreeSet<DataNode>>();
		param_map = new HashMap<String, Map>();
	}
	
	public static Finder getInstance() {
		if( currentFinder != null ) {
			return currentFinder;
		}
		currentFinder = new Finder();
		return currentFinder;
	}
	public void setGoToIndex(int offset) {
		goToOffset = offset;
	}
	
	public void setGoToFunc(String name) {
		goToName = name;
	}
	
	public int getGoToIndex() {
		if(goToOffset > 0) {
			return goToOffset;
		}
		return -1;
	}
		
	public String getGoToFunc() {
		if(goToName != null) {
			return goToName;
		}
		return null;
	}
	public static void add( DataNode dn ) {
		//System.out.println(dn.getSignature() + " " + dn.getStartPosition());
		TreeSet<DataNode> list;
		String key = dn.getValue();
		if (!map.containsKey(key)) {
			//System.out.println(dn.getSignature() + " " + dn.getStartPosition());
			list = new TreeSet<DataNode>();
			list.add(dn);
			map.put(key, list);
		}
		else {
			list = map.get(key);
			list.add(dn);
			map.put(key, list);
		}
	}
	
	public void addParameter(DataNode dn, SimpleName name) {
		//System.out.println(dn.getValue()+" "+name.getIdentifier()+" "+dn.getType());
		ArrayList<SimpleName> list;
		Map<String, ArrayList<SimpleName>> items;
		String key = dn.getValue();
		if (!param_map.containsKey(key)) {
			items = new HashMap<String, ArrayList<SimpleName>>();
			list = new ArrayList<SimpleName>();
			list.add(name);
			items.put(dn.getType(), list);
			param_map.put(key, items);
		}
		else {
			items = param_map.get(key);
			if(!items.containsKey(dn.getType())) {
				list = new ArrayList<SimpleName>();
			}
			else {
				list = items.get(dn.getType());
			}
			list.add(name);
			items.put(dn.getType(), list);
			param_map.put(key, items);
		}
	}
	
	public static ArrayList<String> getParamMethodNames(String key, String direction) {
		ArrayList<String> list = new ArrayList<String>();
		if(!param_map.containsKey(key)) {
			return null;
		}
		else if(!param_map.get(key).containsKey(direction)) {
			return null;
		}
		else {
			ArrayList<SimpleName> methods = (ArrayList<SimpleName>) param_map.get(key).get(direction);
			for(SimpleName s: methods) {
				list.add(s.getIdentifier());
			}
			return list;
		}
	}
	
	/**
	 * Function to check if selected text is actually a variable.
	 * @param var
	 * @param index
	 * @param sourceCode
	 * @returns true if text is use of a variable, else false
	 */
	public boolean isVariable(String var, int index, String sourceCode) {
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
	
	/**
	 * This function returns a list of all the places where the current variable is 
	 * initialized which will determine where to highlight in the file.
	 * @param s: Current String
	 * @return ArrayList<ASTNode> of "up" occurrences for current variable name
	 */
	public TreeSet<DataNode> getOccurrences(String s, Position p) {
		//System.out.println(s + " !!! " + p.toString());
		TreeSet<DataNode> returnList = new TreeSet<DataNode>();
		String method = null;
		for(Entry<String, TreeSet<DataNode>> entry : map.entrySet()) {
		    String key = entry.getKey();
		    if( key.equals(s )) {
		    	TreeSet<DataNode> list = entry.getValue();
		    	for( DataNode dn : list ) {
			    	if( dn.getStartPosition() == p.offset ) {
			    		method = dn.getMethod();
				    }
			    }
		    	list = entry.getValue();
		    	for( DataNode dn : list ) {
	    			String newMethod = dn.getMethod();
	    			//TODO distinguish between class and local variables of same name
	    			if( dn.getValue().equals(key) && ( newMethod.equals(method) || newMethod.equals("null") ) ) { 
	    				returnList.add(dn);
	    			}
		    	} 
		    }
		}
		return returnList;	
	}
}
