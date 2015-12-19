/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - initial version
 *******************************************************************************/
package edu.pdx.cs.multiview.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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

import edu.pdx.cs.multiview.util.Debug;


/**
 * !pq: taken from cme tests and further modified (6/18/2005)
 * 
 * The base for this class was taken from the org.eclipse.contribution.junit.test
 * project (Gamma/Beck) and then edited from there.
 */
public class JavaTestProject {
	
	//change for host computer
	public static final String JAVA_IO_LOC = 
						"E:/My Documents/Eclipse Workspaces/gef_workspace/" +
						"edu.pdx.cs.multiview.util/testData/java";
	public static final String TEST_DATA = "E:/My Documents/gef_workspace/" +
									"edu.pdx.cs.multiview.util/testData/foo";

	// projects don't delete reliably whatever we do, so to ensure that
	// every project within a test run is unique, we append a UUID to the
	// project name.
	private static int UNIQUE_ID = 1;
	
	private String name = "";
	private IProject project;
	private IJavaProject javaProject;
	private IPackageFragmentRoot sourceFolder;
	private IFolder binFolder;	
	private BlockingProgressMonitor monitor = new BlockingProgressMonitor();
	
	//private static final String RT_JAR = "/opt/jdk1.4.2_04/jre/lib/rt.jar";
	//private static final String ASPECTJRT_JAR = "/home/colyer/aspectj1.2/lib/aspectjrt.jar";
	
	public JavaTestProject() throws CoreException {
		this("TestProject","bin",true);
	}
	
	public JavaTestProject(String pname) throws CoreException {
		this(pname,"bin",true);
	}
	
	public JavaTestProject(String pname, boolean createContent) throws CoreException {
		this(pname,"bin",createContent);
	}

	public JavaTestProject(String pname,String outputfoldername) throws CoreException {
		this(pname,outputfoldername,true);
	}
	
	public JavaTestProject(String pname,String outputfoldername,boolean createContent) throws CoreException {
		IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
		name = pname + UNIQUE_ID++;
		project= root.getProject(name);
		monitor.reset();
		if(project.exists())
			project.delete(true,null);
		project.create(monitor);
		monitor.waitForCompletion();
		monitor.reset();
		project.open(monitor);
		monitor.waitForCompletion();
		javaProject = JavaCore.create(project);
		waitForJobsToComplete(project);
		setJavaNature();
		
		binFolder = createOutputFolder(outputfoldername);
		waitForJobsToComplete(project);		
		createOutputFolder(binFolder);
		waitForJobsToComplete(project);
		
		monitor.reset();
		javaProject.setRawClasspath(new IClasspathEntry[0], monitor);
		monitor.waitForCompletion();
			
		if (createContent) {
			addSystemLibraries();
		}
	}

	private void setJavaNature() throws CoreException {
		IProjectDescription description= project.getDescription();
		description.setNatureIds(new String[] { JavaCore.NATURE_ID });
		project.setDescription(description, null);
	}
	public String getName() {
		return name;
	}
	
	public IProject getProject() {
		return project;
	}

	public IJavaProject getJavaProject() {
		return javaProject;
	}
	
	public void addJar(String plugin, String jar) throws MalformedURLException, IOException, JavaModelException {
		Path result= findFileInPlugin(plugin, jar);
		IClasspathEntry[] oldEntries= javaProject.getRawClasspath();
		IClasspathEntry[] newEntries= new IClasspathEntry[oldEntries.length + 1];
		System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
		newEntries[oldEntries.length]= JavaCore.newLibraryEntry(result, null, null);
		javaProject.setRawClasspath(newEntries, null);
	}

	public IPackageFragment createPackage(String pname) throws CoreException {
		if (sourceFolder == null)
			sourceFolder= createSourceFolder();
		monitor.reset();
		IPackageFragment ret = sourceFolder.createPackageFragment(pname, false, monitor);
		monitor.waitForCompletion();
		return ret;
	}

	public IType createType(IPackageFragment pack, String cuName, String source) throws JavaModelException {
		StringBuffer buf= new StringBuffer();
		buf.append("package " + pack.getElementName() + ";\n");
		buf.append("\n");
		buf.append(source);
		monitor.reset();
		ICompilationUnit cu= pack.createCompilationUnit(cuName, buf.toString(), false, monitor);
		monitor.waitForCompletion();
		return cu.getTypes()[0];
	}

	public IPackageFragmentRoot getSourceFolder() throws CoreException {
		if (sourceFolder == null)
			sourceFolder= createSourceFolder();
		return sourceFolder;
	}
	
	public IFolder createFolder(IPackageFragmentRoot root, String fname) throws JavaModelException, CoreException {
		IFolder folder = (IFolder) root.getCorrespondingResource();
		IFolder ret = folder.getFolder(fname);
		if (!folder.exists()) {
			monitor.reset();
			ret.create(true,true,monitor);
			monitor.waitForCompletion();
		}
		return ret;
	}
	
	public IFile createFile(IFolder inFolder,String fname, String content) throws CoreException {
		IFile file = inFolder.getFile(fname);
		if (file.exists()) {
			file.delete(0,null);
		}
		ByteArrayInputStream source = new ByteArrayInputStream(content.getBytes());
		file.create(source,true,null);		
		return file;
	}
	
	public IFolder getOutputFolder() {
		return binFolder;
	}
	
	public synchronized void dispose() throws CoreException {
		waitForIndexer();
		monitor.reset();
//!pq		try {
			project.delete(IResource.ALWAYS_DELETE_PROJECT_CONTENT, monitor);
//		} catch (ResourceException re) {
//			// ignore this
//		}
		monitor.waitForCompletion();
	}
	
//	public String run(String className) {
//		StringBuffer output = new StringBuffer();
//		try {
//			File binDir = new File(binFolder.getLocation().toOSString());
//			Process p = Runtime.getRuntime().exec("java -classpath " +
//							binDir.getPath() + ":" + ASPECTJRT_JAR + " " +  className,
//							null,new File(binFolder.getLocation().toOSString()));
//			InputStream is = p.getInputStream();
//			InputStreamReader isr = new InputStreamReader(is);
//			p.waitFor();
//			int c;
//			while ((c = isr.read()) != -1) {
//				output.append((char)c);
//			}
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//		return output.toString();
//	}

	private synchronized IFolder createOutputFolder(String outputfoldername) throws CoreException {
		IFolder folder= project.getFolder(outputfoldername);
		if (!folder.exists()) {
			monitor.reset();
//!pq			try {
				folder.create(true, true, monitor);
//			} catch (ResourceException e) {
//				// we don't care about this
//			}
			monitor.waitForCompletion();
		}
		return folder;
	}
	
	private void createOutputFolder(IFolder folder) throws JavaModelException {
		IPath outputLocation= folder.getFullPath();
		//monitor.reset(); doesn't always use monitor...
		javaProject.setOutputLocation(outputLocation, monitor);
		monitor.waitForCompletion();
	}

	private IPackageFragmentRoot createSourceFolder() throws CoreException {
		IFolder folder= project.getFolder("src");
		if (!folder.exists()) {
			monitor.reset();
			folder.create(false, true, monitor);
			monitor.waitForCompletion();
		}
		IPackageFragmentRoot root= javaProject.getPackageFragmentRoot(folder);

		IClasspathEntry[] oldEntries= javaProject.getRawClasspath();
		IClasspathEntry[] newEntries= new IClasspathEntry[oldEntries.length + 1];
		System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
		newEntries[oldEntries.length]= JavaCore.newSourceEntry(root.getPath());
		monitor.reset();
		javaProject.setRawClasspath(newEntries, monitor);
		monitor.waitForCompletion();
		return root;
	}

	private void addSystemLibraries() throws JavaModelException {
		IClasspathEntry[] oldEntries= javaProject.getRawClasspath();
		IClasspathEntry[] newEntries= new IClasspathEntry[oldEntries.length + 1];
		System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
		newEntries[oldEntries.length]= JavaRuntime.getDefaultJREContainerEntry();
//		newEntries[oldEntries.length]= JavaCore.newLibraryEntry(new Path(RT_JAR),null,null);
//		newEntries[oldEntries.length+1]= JavaCore.newLibraryEntry(new Path(ASPECTJRT_JAR),null,null);
		monitor.reset();
		javaProject.setRawClasspath(newEntries, monitor);
		monitor.waitForCompletion();

//		IClasspathEntry[] oldEntries= javaProject.getRawClasspath();
//		IClasspathEntry[] newEntries= new IClasspathEntry[oldEntries.length + 1];
//		System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
//		newEntries[oldEntries.length]= JavaRuntime.getDefaultJREContainerEntry();
//		javaProject.setRawClasspath(newEntries, null);
	}

	private Path findFileInPlugin(String plugin, String file) throws MalformedURLException, IOException {
		org.osgi.framework.Bundle bundle = Platform.getBundle(plugin);
		URL pluginURL = bundle.getEntry("/");		
		URL jarURL= new URL(pluginURL, file);
		URL localJarURL= Platform.asLocalURL(jarURL);
		return new Path(localJarURL.getPath());
	}

	//version fetched from the contributing to eclipse mailing list
	private void waitForIndexer() throws JavaModelException {
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

  public static class BlockingProgressMonitor implements IProgressMonitor {

	private Boolean isDone = Boolean.FALSE;
	
	public boolean isDone() {
		boolean ret = false;
		synchronized(isDone) {
		  ret = (isDone == Boolean.TRUE);
		}
		return ret;
	}
	
	public void reset() {
		synchronized(isDone) {
		  isDone = Boolean.FALSE;
		}
	}
	
	public void waitForCompletion() {
		while (!isDone()) {
			try {
				synchronized(this) { wait(500); }
			} catch (InterruptedException intEx) {
				// no-op
			}
		}
	}
	
	public void beginTask(String name, int totalWork) {
		if (name != null) Debug.trace(name);
		reset();
	}

	public void done() {
		synchronized(isDone) {
			isDone = Boolean.TRUE;
		}
		synchronized(this) {
			notify();			
		}
	}

	public void internalWorked(double work) {
	}

	public boolean isCanceled() {
		return false;
	}

	public void setCanceled(boolean value) {
	}

	public void setTaskName(String name) {
	}

	public void subTask(String name) {
	}

	public void worked(int work) {
	}
  }	
  
	public static void waitForJobsToComplete(IProject pro){
		Job job = new Job("Dummy Job"){
			@Override
			public IStatus run(IProgressMonitor m){
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.DECORATE);
		job.setRule(pro);
	    job.schedule();
	    try {
			job.join();
		} catch (InterruptedException e) {
			// Do nothing
		}
	}



	/**
	 * Copy the files at this location into the source directory of this project.
	 * @param location - the location
	 * @throws CoreException 
	 */
	public void copyToSourceDir(String location) throws Exception {
		File source = new File(location);
		if (!source.exists())
			throw new IllegalArgumentException("location does not exist");
		IPackageFragmentRoot root = getSourceFolder();
		IResource resource = root.getResource();
		IPath rawLocation = resource.getRawLocation();
		File destination = rawLocation.toFile();
		copy(source, destination);
		getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
		waitForJobsToComplete(getProject());
	}

	private void copy(File source, File destination) throws Exception {
		if (source.isFile())
			copyFileToFolder(source, destination);
		else
			copyFolderToFolder(source, destination);
	}
  

	private void copyFolderToFolder(File sourceDir, File destination) throws Exception {
		if (sourceDir.isFile())
			throw new IllegalArgumentException("source is not a directory");
		File newDir = new File(destination, sourceDir.getName());
		newDir.mkdir();
		File[] files = sourceDir.listFiles();
		for (File file : files) {
			copy(file, newDir);
		}
	}

	private void copyFileToFolder(File source, File destination) throws Exception {
		File newFile = new File(destination, source.getName());
		newFile.createNewFile();
		FileChannel srcChannel  = null;
		FileChannel dstChannel = null;
		try {
	        srcChannel = new FileInputStream(source).getChannel();
	        dstChannel = new FileOutputStream(newFile).getChannel();
	    	dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
	    } finally {
	        srcChannel.close();
	        dstChannel.close();
	    }}
	
	public IType getType(String desiredType, String pkg) 
				throws CoreException, JavaModelException {
		IPackageFragment frag = getSourceFolder().getPackageFragment(pkg);
		IType writer = null;
		for(IJavaElement child : frag.getChildren()){
			if(child.getElementName().equals(desiredType+".java"))
				writer = ((ICompilationUnit)child).findPrimaryType();
		}
		return writer;
	}
	}
