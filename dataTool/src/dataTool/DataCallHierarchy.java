package dataTool;

import java.util.ArrayList;
import java.util.HashSet;

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
	
	private String searchMatch;
	private String method;
	private String parameters;
	private String className;
	private String location;
	private ICompilationUnit cUnit;
	
	public DataCallHierarchy() {
		
	}
	
	public HashSet<IMethod> getCalls(IMethod m) {
		Finder f = Finder.getInstance();
		if(f instanceof UpFinder) {
			return getCallersOf(m);
		}
		else if(f instanceof DownFinder) {
			return getCalleesOf(m);
		}
		return null;
	}
	
	public HashSet<IMethod> getCalleesOf(IMethod m) {
		CallHierarchy callHierarchy = CallHierarchy.getDefault();
		 
		IMember[] members = {m};
		MethodWrapper[] methodWrappers = callHierarchy.getCalleeRoots(members);
		HashSet<IMethod> callers = new HashSet<IMethod>();
		for (MethodWrapper mw : methodWrappers) {
		    MethodWrapper[] mw2 = mw.getCalls(new NullProgressMonitor());
		    HashSet<IMethod> temp = getIMethods(mw2);
		    callers.addAll(temp);    
		} 
		return callers;
	}
	
	public HashSet<IMethod> getCallersOf(IMethod m) {
		 CallHierarchy callHierarchy = CallHierarchy.getDefault();
		 
		 IMember[] members = {m};
		 
		 MethodWrapper[] methodWrappers = callHierarchy.getCallerRoots(members);
		 HashSet<IMethod> callers = new HashSet<IMethod>();
		 for (MethodWrapper mw : methodWrappers) {
		     MethodWrapper[] mw2 = mw.getCalls(new NullProgressMonitor());
		     HashSet<IMethod> temp = getIMethods(mw2);
		     callers.addAll(temp);    
		 }
		return callers;
	}
		 
	private HashSet<IMethod> getIMethods(MethodWrapper[] methodWrappers) {
		  HashSet<IMethod> c = new HashSet<IMethod>(); 
		  for (MethodWrapper m : methodWrappers) {
			  IMethod im = getIMethodFromMethodWrapper(m);
			  if (im != null) {
				  c.add(im);
			  }
		  }
		  return c;
	}
		 
	private IMethod getIMethodFromMethodWrapper(MethodWrapper m) {
		try {
		    IMember im = m.getMember();
		    if (im.getElementType() == IJavaElement.METHOD) {
		    	return (IMethod)m.getMember();
		    }
		} 
		catch (Exception e) {
		    e.printStackTrace();
		 }
		 return null;
	}
	
	/**
	 * Search project for method
	 * @param project: String project name
	 * @param method: String method name
	 * @returns String of next method name
	 * @throws CoreException
	 */
	public ArrayList<DataLink> searchProject(String method) throws CoreException {
		ArrayList<DataLink> results = new ArrayList<DataLink>();
		String projectName = EnableNavigationAction.project;
		String path = EnableNavigationAction.path;
		String projectPath = path.substring(path.indexOf("/src/")+5,path.lastIndexOf("/")).replace("/", ".");
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
	    HashSet<IMethod> calls = null;
	    for(IMethod im: type.getMethods()){
	    	if(im.getElementName().equals(method)) {
	    		calls = getCalls(im);
	    		for(IMethod mem: calls) {
	    			//results += mem.getElementName() + "\n";
	    			results.add(new DataLink(mem.getElementName(), mem.toString()));
	    			searchMatch = mem.toString();
	    		}
	    	}
	    }
	    if(calls == null) {
	    	results.add(processRootDirectory(method));
	    }
	    return results;
	}
	
	/**
	 * Function to parse the output from the search function above
	 * @param s: String of search results
	 */
	private void parseSearchResults(String s) {
		System.out.println(s);
		method = s.substring(s.indexOf(" ")+1,s.indexOf("("));
		parameters = s.substring(s.indexOf("("),s.indexOf(")")+1);
		String temp[] = s.substring(s.indexOf("[")).split("\\[in ");
		String tempLoc = "";
		for(int i = 1; i < temp.length; i++) {
			String str = temp[i].trim().replace("[Working copy] ","").replace("]","");
			if(i==1) {
				className = str;
				continue;
			}
			tempLoc = str+"."+tempLoc;
		}
		location = tempLoc.substring(0,tempLoc.length()-1);
	}
	
	private DataLink processRootDirectory(String method) throws JavaModelException,CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();		
		IProject[] projects = root.getProjects();
		
		// process each project
		for (IProject project : projects) {
			if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
				IJavaProject javaProject = JavaCore.create(project);
				IPackageFragment[] packages = javaProject.getPackageFragments();
		
				// process each package
				for (IPackageFragment pack: packages) {
					// We will only look at the package from the source folder
					// K_BINARY would include also included JARS
					// only process the .java files
					if (pack.getKind() == IPackageFragmentRoot.K_SOURCE) {
						for (ICompilationUnit unit : pack.getCompilationUnits()) {
							IType[] allTypes = unit.getAllTypes();
							for (IType type : allTypes) {
								IMethod[] methods = type.getMethods();
								for (IMethod imethod : methods) {
									if(imethod.getElementName().equals(method)) {
										searchMatch = method.toString();
										return new DataLink(imethod.getElementName(), imethod.toString());
									}
								}
							}
						}
					}
				}
			}

		}
		return new DataLink(DataLink.INVALID, DataLink.INVALID_DESC);
	}
}
