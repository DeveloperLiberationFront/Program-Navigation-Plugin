package dataTool;

import java.util.ArrayList;
import java.util.Collection;
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
	protected static Map<String, TreeSet<DataNode>> map; // contains name and
															// first node for
															// all data
	/** Maps a method's declaration to the list of Methods invoked inside of its body. */
	public HashMap<Method, ArrayList<Method>> declarationToInvocationMapDown = new HashMap<Method, ArrayList<Method>>();
	/** 
	 * Maps a method's invocation to the list of methods it is invoked in. 
	 * 
	 * Maps the method's SimpleName.resolveBinding().toString() to the list of Method Objects where that
	 * method is invoked.
	 */
	public HashMap<String, ArrayList<Method>> invocationToDeclarationMapUp = new HashMap<String, ArrayList<Method>>();
	final public static String UP = "up";
	final public static String DOWN = "down";

	private static Finder currentFinder;
	private String goToName = null;
	private int goToOffset = -1;

	private Finder() {
		map = new HashMap<String, TreeSet<DataNode>>();
	}

	public static Finder getInstance() {
		if (currentFinder != null) {
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
		if (goToOffset > 0) {
			return goToOffset;
		}
		return -1;
	}

	public String getGoToFunc() {
		if (goToName != null) {
			return goToName;
		}
		return null;
	}

	public static void add(DataNode dn) {
		TreeSet<DataNode> list;
		String key = dn.getBinding();
		if (!map.containsKey(key)) {
			list = new TreeSet<DataNode>();
			list.add(dn);
			map.put(key, list);
		} else {
			list = map.get(key);
			list.add(dn);
			map.put(key, list);
		}
	}

//	public void addParameter(DataNode dn, Method method) {
//		// "+dn.getType());
//		HashSet<Method> list;
//		Map<String, HashSet<Method>> items;
//		String key = dn.getValue();
//		if (!param_map.containsKey(key)) {
//			items = new HashMap<String, HashSet<Method>>();
//			list = new HashSet<Method>();
//			list.add(method);
//			items.put(dn.getType(), list);
//			param_map.put(key, items);
//		} else {
//			items = param_map.get(key);
//			if (!items.containsKey(dn.getType())) {
//				list = new HashSet<Method>();
//			} else {
//				list = items.get(dn.getType());
//			}
//			list.add(method);
//			items.put(dn.getType(), list);
//			param_map.put(key, items);
//		}
//	}
//
//	public static ArrayList<String> getParamMethodNames(String key, String direction) {
//		ArrayList<String> list = new ArrayList<String>();
//		if (!param_map.containsKey(key)) {
//			return null;
//		} else if (!param_map.get(key).containsKey(direction)) {
//			return null;
//		} else {
//			HashSet<Method> methods = param_map.get(key).get(direction);
//			for (Method m : methods) {
//				list.add(m.getName().getIdentifier());
//			}
//			return list;
//		}
//	}

	/**
	 * Function to check if selected text is actually a variable.
	 * 
	 * @param var
	 * @param index
	 * @param sourceCode
	 * @returns true if text is use of a variable, else false
	 */
	public boolean isVariable(String var, int index, String sourceCode) {
		boolean check = false;
		if (sourceCode.substring(index + var.length(), index + var.length() + 1).matches("[a-zA-Z0-9]")
				|| sourceCode.substring(index - 1, index).matches("[a-zA-Z0-9]")) {
			return false;
		}
		if (isComment(var, index, sourceCode)) {
			return false;
		}
		return true;
	}

	/**
	 * Checks to see if the visited variable name is inside a comment so we
	 * don't highlight it. Kind of a hack
	 * 
	 * @param var:
	 *            String name of the data
	 * @param index:
	 *            int start position of the data
	 * @param sourceCode:
	 *            String of the entire code
	 * @returns true if name is within a comment, else false
	 */
	private boolean isComment(String var, int index, String sourceCode) {
		// Check if line starts with //, /*, /**, or *
		String temp = sourceCode.substring(0, index);
		if (temp.lastIndexOf("/**") > temp.lastIndexOf("*/") || temp.lastIndexOf("/*") > temp.lastIndexOf("*/")
				|| temp.lastIndexOf("//") > temp.lastIndexOf("\n")) {
			return true;
		}
		return false;
	}

	/**
	 * This function returns a list of all the places where the current variable
	 * is initialized which will determine where to highlight in the file.
	 * 
	 * @param s:
	 *            Current String
	 * @return ArrayList<ASTNode> of "up" occurrences for current variable name
	 */
	public TreeSet<DataNode> getOccurrences(String s, Position p) {
		TreeSet<DataNode> returnList = new TreeSet<DataNode>();
		String binding = null;
		String key = null;

		for (Entry<String, TreeSet<DataNode>> entry : map.entrySet()) {
			key = entry.getKey();
			// Gets rid of the type declaration
			key = key.substring(key.indexOf(" ") + 1);
			DataNode foundNode = null;
			// For regular fields
			if (key.startsWith(s)) {
				TreeSet<DataNode> list = entry.getValue();
				for (DataNode dn : list) {
					if (dn.getStartPosition() == p.offset) {
						foundNode = dn;
						binding = dn.getBinding();
						break;
					}
				}
				list = entry.getValue();
				for (DataNode dn : list) {
					// TODO distinguish between class and local variables of
					// same name
					if (dn.getBinding().equals(binding)) {
						returnList.add(dn);
					}
				}
				// For class variables
			} else if (key.endsWith(s)) {
				TreeSet<DataNode> list = entry.getValue();
				for (DataNode dn : list) {
					if (dn.getStartPosition() == p.offset) {
						binding = dn.getBinding();
						break;
					}
				}
				list = entry.getValue();
				for (DataNode dn : list) {
					// TODO distinguish between class and local variables of
					// same name
					if (dn.getBinding().equals(binding)) {
						returnList.add(dn);
					}
				}
			}
		}
		return returnList;
	}
	
	public ArrayList<Method> upSearch( DataNode dn ) {
		System.out.println(dn.getDeclarationMethod());
		if(dn.getDeclarationMethod() != null) {
			String declarationMethodBinding = dn.getDeclarationMethod().getName().resolveBinding().toString();
			System.out.println(invocationToDeclarationMapUp.entrySet());
			return invocationToDeclarationMapUp.get(declarationMethodBinding);
		}
		return null;
	}
	
	public ArrayList<Method> downSearch( DataNode dn ) {
		return declarationToInvocationMapDown.get(dn.getDeclarationMethod());
	}

	public void setInvocationToDeclarationMap(
			HashMap<String, ArrayList<Method>> invocationToDeclarationStringMapUp) {
		this.invocationToDeclarationMapUp = invocationToDeclarationStringMapUp;
		
	}

	public void setDeclarationToInvocationMap(HashMap<Method, ArrayList<Method>> declarationToInvocationMapDown) {
		this.declarationToInvocationMapDown = declarationToInvocationMapDown;
		
	}
}
