package dataTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
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

import dataTool.ui.DataLink;


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
	private HashMap<String, ArrayList<DataLink>> results;
	
	public DataCallHierarchy() {
		
	}	
	
	/**
	 * Searches project for given method name
	 * @param methodName: String of current method name
	 * @param direction: String we are searching (Finder.UP or Finder.DOWN)
	 * @returns Set of methods calling or called by methodName
	 * @throws JavaModelException
	 */
	public Set<IMethod> search(String methodName, String direction) throws JavaModelException {
	    CallHierarchyGenerator callGen = new CallHierarchyGenerator();
	    projectName = EnableNavigationAction.project;
		String path = EnableNavigationAction.path;
		String projectPath;
		if(path.contains("/src/")) {
			projectPath = path.substring(path.indexOf("/src/")+5,path.lastIndexOf("/")).replace("/", ".");
		}
		else {
			projectPath = path;
		}
		String projectFile = EnableNavigationAction.file;
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
	    IProject project = root.getProject(projectName);
	    IFolder folder = project.getFolder("src");
	    IJavaProject javaProject = JavaCore.create(project);
	    IPackageFragmentRoot ipf = javaProject.getPackageFragmentRoot(folder);
	    ipf.createPackageFragment(projectPath, true, null);
	    IPackageFragment frag = ipf.getPackageFragment(projectPath);
	    cUnit = frag.getCompilationUnit(projectFile);
	    IType type = cUnit.getType(projectFile.replace(".java", ""));
		IMethod m = findMethod(type, methodName);
	    Set<IMethod> methods = new HashSet<IMethod>();
	    if(direction.equals(Finder.UP)) {
		    methods = callGen.getCallersOf(m);
	    }
	    else {
		    Set<IMethod> temp = callGen.getCalleesOf(m);
		    for(IMethod i: temp) {
		    	methods.add(i);
		    }
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
	            theMethod = imethod;
	        }
	    }

	    if (theMethod == null) {           
	        System.out.println("Error, method " + methodName + " not found");
	        return null;
	    }

	    return theMethod;
	}
}
