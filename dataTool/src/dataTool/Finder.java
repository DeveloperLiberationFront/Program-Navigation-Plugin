package dataTool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.lang.model.SourceVersion;

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
	private static HashMap<Position, String> positionBindingMap;
	protected static Map<String, TreeSet<DataNode>> bindingListMap; // contains name and
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
		positionBindingMap = new HashMap<Position, String>();
		bindingListMap = new HashMap<String, TreeSet<DataNode>>();
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
		String key = dn.getKey();
		positionBindingMap.put( dn.getPosition(), dn.getKey() );
		if (!bindingListMap.containsKey(key)) {
			list = new TreeSet<DataNode>();
			list.add(dn);
			bindingListMap.put(key, list);
		} else {
			list = bindingListMap.get(key);
			list.add(dn);
			bindingListMap.put(key, list);
		}
	}

	/**
	 * This function returns a list of all the places where the current variable
	 * is initialized which will determine where to highlight in the file.
	 * 
	 * @param s:
	 *            Current String
	 * @return ArrayList<ASTNode> of "up" occurrences for current variable name
	 */
	public TreeSet<DataNode> getOccurrences(Position p) {
			String binding = positionBindingMap.get(p);
			TreeSet<DataNode> returnList = new TreeSet<DataNode>();
			if( binding != null ) {
				returnList.addAll(bindingListMap.get(binding));
			}
			return returnList;
//		} 
	}
	
	public ArrayList<Method> upSearch( DataNode dn ) {
		if(dn.getDeclarationMethod() != null) {
			String declarationMethodBinding = dn.getDeclarationMethod().getName().resolveBinding().toString();
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