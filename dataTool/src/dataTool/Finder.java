package dataTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
	protected static Map<String, Map<String, HashSet<Method>>> param_map;
	protected static Map<Method, Method> transitionUpMap;
	protected static Map<Method, Method> transitionDownMap;
	final public static String UP = "up";
	final public static String DOWN = "down";
	
	private static Finder currentFinder;
	private String goToName = null;
	private int goToOffset = -1;
	
	private Finder () {
		map = new HashMap<String, TreeSet<DataNode>>();
		param_map = new HashMap<String, Map<String, HashSet<Method>>>();
		transitionUpMap = new HashMap<Method, Method>();
		transitionDownMap = new HashMap<Method, Method>();
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
	
	public void addParameter(DataNode dn, Method method) {
		//System.out.println(dn.getValue()+" "+name.getIdentifier()+" "+dn.getType());
		HashSet<Method> list;
		Map<String, HashSet<Method>> items;
		String key = dn.getValue();
		if (!param_map.containsKey(key)) {
			items = new HashMap<String, HashSet<Method>>();
			list = new HashSet<Method>();
			list.add(method);
			items.put(dn.getType(), list);
			param_map.put(key, items);
		}
		else {
			items = param_map.get(key);
			if(!items.containsKey(dn.getType())) {
				list = new HashSet<Method>();
			}
			else {
				list = items.get(dn.getType());
			}
			list.add(method);
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
			HashSet<Method> methods = param_map.get(key).get(direction);
			for(Method m: methods) {
				list.add(m.getName().getIdentifier());
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
		DataNode currentDataNode = null;
		TreeSet<DataNode> list = map.get(s);
    	// Determine the signature of the method currently in
		// Takes care of overloaded variable names
    	for( DataNode dn : list ) {
	    	if( dn.getStartPosition() == p.offset ) {
	    		currentDataNode = dn;
	    		break;
		    }
	    }
    	if( currentDataNode != null ) {
    		// Add all nodes in current method to return list
    		String key = currentDataNode.getValue();
    		String method = currentDataNode.getMethodSignature();
    		// The list of nodes in this method with that variable name
        	for( DataNode dn : list ) {
    			String newMethod = dn.getMethodSignature();
    			//To distinguish between class and local variables of same name
    			if( dn.getValue().equals(key) && ( newMethod.equals(method) || newMethod.equals("null") ) ) { 
    				returnList.add(dn);
    			}
        	} 
        	Method currentMethod = currentDataNode.getMethod();
        	List<DataNode> args= currentMethod.getArgs();
        	int argIndex = -1;
        	for( DataNode arg: args) {
        		argIndex++;
        		if( arg.getValue().equals(s) ) {
        			// The provided variable is a parameter of the method it is in
        			// I'm sure there is a better way to do this.
        			break;
        		}
        	}
        	// At this point, the desired node is a parameter of the method
        	// Need to search up.
        	if( currentMethod != null && argIndex > -1) {
        		Method searchMethod = null;
        		searchMethod = transitionDownMap.get(currentMethod);
        		System.out.println(currentMethod.getSignature());
        		DataNode newDataNode = searchMethod.getArgs().get(argIndex);
        		returnList.addAll( this.getOccurrences(newDataNode));
        	}
    	}
    	
		return returnList;	
		
	}
	
	private TreeSet<DataNode> getOccurrences( DataNode currentDataNode ) {
		TreeSet<DataNode> returnList = new TreeSet<DataNode>();
		TreeSet<DataNode> list = map.get(currentDataNode.getValue());
		String key = currentDataNode.getValue();
		String method = currentDataNode.getMethodSignature();
		// The list of nodes in this method with that variable name
    	for( DataNode dn : list ) {
			String newMethod = dn.getMethodSignature();
			//To distinguish between class and local variables of same name
			if( dn.getValue().equals(key) && ( newMethod.equals(method) || newMethod.equals("null") ) ) { 
				returnList.add(dn);
			}
    	} 
    	Method currentMethod = currentDataNode.getMethod();
    	List<DataNode> args= currentMethod.getArgs();
    	int argIndex = -1;
    	for( DataNode arg: args) {
    		argIndex++;
    		if( arg.getValue().equals(currentDataNode.getValue()) ) {
    			// The provided variable is a parameter of the method it is in
    			// I'm sure there is a better way to do this.
    			break;
    		}
    	}
    	// At this point, the desired node is a parameter of the method
    	// Need to search up.
    	if( currentMethod != null && argIndex > -1) {
    		Method searchMethod = null;
    		searchMethod = transitionDownMap.get(currentMethod);
    		DataNode newDataNode = searchMethod.getArgs().get(argIndex);
    		returnList.addAll( this.getOccurrences(newDataNode ));
    	}
		return returnList;
	}
	public void addDownTransition(Method method, Method invoked) {
		transitionDownMap.put( method, invoked);
	}

	public void addUpTransition(Method invoked, Method method) {
		transitionUpMap.put( invoked, method );
	}
}
