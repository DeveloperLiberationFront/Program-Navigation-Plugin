package edu.pdx.cs.multiview.test;

/*
 * Created on Jun 1, 2004
 */


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameRequestor;
import org.eclipse.jdt.launching.JavaRuntime;
import org.osgi.framework.Bundle;

/**
 * @author BeckwithW
 * @deprecated use JavaTestProject instead
 */
@Deprecated
public class TestProject
{
	private IProject project;
	private IJavaProject javaProject;
	private IPackageFragmentRoot sourceFolder;
	
	/**
	 * @throws CoreException
	 */
	public TestProject(String name) throws CoreException
	{
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		project = root.getProject(name);
		project.create(null);
		project.open(null);
		javaProject = JavaCore.create(project);
		IFolder binFolder = createBinFolder();
		setJavaNature();
		javaProject.setRawClasspath(new IClasspathEntry[0], null);
		createOutputFolder(binFolder);
		addSystemLibraries();
	}

	/**
	 * @throws CoreException
	 */
	public void buildProject() throws CoreException
	{
		project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
	}
	
	/**
	 * 
	 */
	public void joinBuild()
	{
		try
		{
			Platform.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD,
				null);
		}
		catch (InterruptedException e)
		{
			// TODO: handle exception
		}
		catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	/**
	 * @return Returns the javaProject.
	 */
	public IJavaProject getJavaProject()
	{
		return javaProject;
	}

	/**
	 * @param javaProject
	 *            The javaProject to set.
	 */
	public void setJavaProject(IJavaProject javaProject)
	{
		this.javaProject = javaProject;
	}

	/**
	 * @return Returns the project.
	 */
	public IProject getProject()
	{
		return project;
	}

	/**
	 * @param project
	 *            The project to set.
	 */
	public void setProject(IProject project)
	{
		this.project = project;
	}

	/**
	 * @param plugin
	 * @param jarfile
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws JavaModelException
	 */
	public void addJar(String plugin, String jarfile)
		throws MalformedURLException, IOException, JavaModelException
	{
		IPath result = findFileInPlugin(plugin, jarfile);
		IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
		IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
		System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
		newEntries[oldEntries.length] = JavaCore.newLibraryEntry(result, null,
			null);
		javaProject.setRawClasspath(newEntries, null);
	}

	/**
	 * @param name
	 * @return
	 * @throws CoreException
	 */
	public IPackageFragment createPackage(String name) throws CoreException
	{
		if (sourceFolder == null)
		{
			sourceFolder = createSourceFolder();
		}
		return sourceFolder.createPackageFragment(name, false, null);
	}

	/**
	 * @param packageFragment
	 * @param cuName
	 * @param source
	 * @return
	 * @throws JavaModelException
	 */
	public IType createType(IPackageFragment packageFragment, String cuName,
		String source) throws JavaModelException
	{
		StringBuffer buffer = new StringBuffer(80);
		buffer.append("package ");
		buffer.append(packageFragment.getElementName());
		buffer.append(";\n");
		buffer.append("\n");
		buffer.append(source);
		ICompilationUnit compUnit = packageFragment.createCompilationUnit(
			cuName, buffer.toString(), false, null);
		return compUnit.getTypes()[0];
	}

	/**
	 * @throws CoreException
	 */
	public void dispose() throws CoreException
	{
		waitForIndexer();
		project.delete(true, true, null);
	}

	/**
	 * @return
	 * @throws CoreException
	 */
	private IFolder createBinFolder() throws CoreException
	{
		IFolder binFolder = project.getFolder("bin");
		binFolder.create(false, true, null);
		return binFolder;
	}

	/**
	 * @throws CoreException
	 */
	private void setJavaNature() throws CoreException
	{
		IProjectDescription description = project.getDescription();
		description.setNatureIds(new String[]{JavaCore.NATURE_ID});
		project.setDescription(description, null);
	}

	/**
	 * @param binFolder
	 * @throws JavaModelException
	 */
	private void createOutputFolder(IFolder binFolder)
		throws JavaModelException
	{
		IPath outputLocation = binFolder.getFullPath();
		javaProject.setOutputLocation(outputLocation, null);
	}

	/**
	 * @return
	 * @throws CoreException
	 */
	private IPackageFragmentRoot createSourceFolder() throws CoreException
	{
		IFolder folder = project.getFolder("src");
		folder.create(false, true, null);
		IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(folder);
		IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
		IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
		System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
		newEntries[oldEntries.length] = JavaCore.newSourceEntry(root.getPath(),
			new IPath[] {});
		javaProject.setRawClasspath(newEntries, null);
		return root;
	}

	/**
	 * @throws JavaModelException
	 */
	private void addSystemLibraries() throws JavaModelException
	{
		IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
		IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
		System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
		newEntries[oldEntries.length] = JavaRuntime
			.getDefaultJREContainerEntry();
		javaProject.setRawClasspath(newEntries, null);
	}

	/**
	 * @param plugin
	 * @param file
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private IPath findFileInPlugin(String plugin, String file)
		throws MalformedURLException, IOException
	{
		Bundle bundle = Platform.getBundle(plugin);
		URL fileURL = bundle.getEntry(file);
		URL localJarURL = Platform.asLocalURL(fileURL);
		IPath filePath = new Path(localJarURL.getPath());
		return filePath;
	}

	/**
	 * @throws JavaModelException
	 */
	private void waitForIndexer() throws JavaModelException
	{
		SearchEngine searchEngine = new SearchEngine();
		TypeNameRequestor typeNameRequestor = new TypeNameRequestor()
		{
			public void acceptClass(char[] packageName, char[] simpleTypeName,
				char[][] enclosingTypeNames, String path)
			{
				// nothing to do
			}

			public void acceptInterface(char[] packageName,
				char[] simpleTypeName, char[][] enclosingTypeNames, String path)
			{
				// nothing to do
			}
		};
		searchEngine.searchAllTypeNames(null, null, SearchPattern.R_EXACT_MATCH
			| SearchPattern.R_CASE_SENSITIVE, IJavaSearchConstants.CLASS,
			SearchEngine.createJavaSearchScope(new IJavaElement[0]),
			typeNameRequestor, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			null);
	}
}