package dataTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchy;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import edu.pdx.cs.multiview.jdt.util.JDTUtils;


/**
 * Class that handles searching the entire project to track the flow of data
 * 
 * @author Chris
 * @source: http://www.programcreek.com/2011/07/find-all-callers-of-a-method/
 * 			http://www.programcreek.com/2012/06/count-total-number-of-methods-in-a-java-project/
 */
public class DataCallHierarchy {
	
	private String projectName;
	private String searchMatch;
	private String method;
	private String parameters;
	private String className;
	private String location;
	private ICompilationUnit cUnit;
	private boolean inClass = false;
	
	public DataCallHierarchy() {
		
	}	
	
	/**
	 * Function to search the project for instances of methods
	 * @param node: Current DataNode selected by user
	 * @param direction: String of data flow from current node
	 * @returns Set of methods
	 * @throws JavaModelException
	 */
	public Set<IMethod> searchProject(DataNode node, String direction) throws JavaModelException {
		Set<IMethod> results = new HashSet<IMethod>();
		//inClass = false;
		if (direction.equals(Finder.UP)) {
			Method up = node.getDeclarationMethod();
			if(up != null) {
				results = search(node, up, Finder.UP);
			}
		}
		else if (direction.equals(Finder.DOWN)) {
			ArrayList<String> down = new ArrayList<String>();
			Set<IMethod> searchDown = new HashSet<IMethod>();
			for(DataNode dn: Finder.getInstance().getOccurrences(node.getPosition())) {
				if(dn.getInvocationMethod() != null) {
					down.add(dn.getInvocationMethod().getName().getIdentifier());
					searchDown.addAll(search(dn, dn.getInvocationMethod(), Finder.DOWN));
				}
			}
			for(IMethod s: searchDown) {
				if(down.contains(s.getElementName())) {
					results.add(s);
				}
			}
		}
		if(results.isEmpty()) {
			return null;
		}
		return results;
	}
	/**
	 * Searches project for given method name
	 * @param node 
	 * @param methodName: String of current method name
	 * @param direction: String we are searching (Finder.UP or Finder.DOWN)
	 * @returns Set of methods calling or called by methodName
	 * @throws JavaModelException
	 */
	public Set<IMethod> search(DataNode node, Method methodName, String direction) throws JavaModelException {
	    CallHierarchyGenerator callGen = new CallHierarchyGenerator();
		IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		String[] pathArray = activeEditor.getTitleToolTip().split("/");
		projectName = pathArray[0];
		String path = activeEditor.getEditorInput().toString().replace("org.eclipse.ui.part.FileEditorInput(", "").replace(")","");
		String projectFile = activeEditor.getTitle();
		String projectPath;
		String src;
		if(path.contains("/src/")) {
			projectPath = path.substring(path.indexOf("/src/")+5,path.lastIndexOf("/")).replace("/", ".");
			src = "src";
		}
		else {
			projectPath = path.substring(1,path.lastIndexOf("/")).replace("/", ".").replace(projectName+".", "");
			src = projectPath.substring(0,projectPath.indexOf("."));
			projectPath = projectPath.replace(src+".", "");
		}
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
	    IProject project = root.getProject(projectName);
	    IFolder folder = project.getFolder(src);
	    IJavaProject javaProject = JavaCore.create(project);
    	IPackageFragmentRoot ipf = javaProject.getPackageFragmentRoot(folder);
    	ipf.createPackageFragment(projectPath, true, null);
	    IPackageFragment frag = ipf.getPackageFragment(projectPath);
	    cUnit = frag.getCompilationUnit(projectFile);
    	IType type = cUnit.getType(projectFile.replace(".java", ""));
		IMethod m = findMethod(type, methodName.getName().getIdentifier());
		if(m == null){
			m = getCurrentMethod(node.getStartPosition());
		}
	    Set<IMethod> methods = new HashSet<IMethod>();
	    if(direction.equals(Finder.UP)) {
		    methods = callGen.getCallersOf(m);
	    }
	    else {
	    	Set<IMethod> temp = callGen.getCalleesOf(m);
			for(IMethod i: temp) {
			  	methods.add(i);
			}
	    	methods.add(m);
	    }
	    return methods;
	}
	
	/**
	 * Finds the method in the current class
	 * @param type: IType for the current class
	 * @param methodName: String name of the method
	 * @returns IMethod of method search or null
	 * @throws JavaModelException
	 */
	private IMethod findMethod(IType type, String methodName) throws JavaModelException
	{
	    IMethod[] methods = type.getMethods();
	    IMethod theMethod = null;

	    for (int i = 0; i < methods.length; i++)
	    {
	        IMethod imethod = methods[i];
	        if (imethod.getElementName().equals(methodName)) {
	        	//inClass = true;
	            theMethod = imethod;
	        }
	    }

	    if (theMethod == null) {           
	        System.out.println("Error, method " + methodName + " not found");
	        return null;
	    }

	    return theMethod;
	}
	
	/**
	 * Gets the method that the cursor is currently located in.
	 * @param offset
	 * @return IMethod for current method
	 */
	public IMethod getCurrentMethod(int offset) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		ITextEditor editor = (ITextEditor) page.getActiveEditor();
		IJavaElement elem = JavaUI.getEditorInputJavaElement(editor.getEditorInput());
		if (elem instanceof ICompilationUnit) {
		    IJavaElement selected = null;
			try {
				selected = ((ICompilationUnit) elem).getElementAt(offset);
			} catch (JavaModelException e) {
				// Auto-generated catch block
				e.printStackTrace();
			}
		    if (selected != null && selected.getElementType() == IJavaElement.METHOD) {
		         return ((IMethod) selected);
		    }
		}
		return null;
	}
}
