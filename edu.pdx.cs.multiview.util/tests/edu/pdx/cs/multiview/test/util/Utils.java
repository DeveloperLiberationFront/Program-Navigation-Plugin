package edu.pdx.cs.multiview.test.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.osgi.framework.Bundle;

import edu.pdx.cs.multiview.test.JavaTestProject;

/**
 * A test environment setup helper.  (Taken from the cme.ui tests.)
 */
public class Utils {

	private static JavaTestProject projectAlpha;
	//ProjectAlpha has a 'bin' directory as the output directory and
	// contains three classes, a HelloWorld class, a Mammal and a Monkey 
	// class - Mammal has a String '_name' field and a getName() accessor and 
	// is extended by Monkey; Monkey overrides getName() and adds a dance() method
	
	private static JavaTestProject projectBeta;
	// ProjectBeta has a 'bindir' directory as the output directory and
	// contains two classes, a HelloWorld class and a Gorilla class - 
	// the Gorilla class is refered to by the Monkey class in Project Alpha
	
	private static JavaTestProject projectGamma;
	// ProjectGamma contains one class - Ape with three methods -  
	// eatBanana(), eatBanana(int) and eatNuts().

	private static JavaTestProject projectOmega;
	//alphabet.X.sum() calls getA() and getB()
	
	private static JavaTestProject projectWithJars;
	// ProjectWithJars exports one jar file - "junit.jar".
	
	public static JavaTestProject createProjectAlpha()
		throws CoreException, JavaModelException {
		projectAlpha = createProject("ProjectAlpha","bin");
		addType_HelloWorld(projectAlpha,"pack1");
		addType_Mammal(projectAlpha,"animals");
		addType_Monkey(projectAlpha,"animals");
		addType_Ape(projectAlpha,"animals");
		addType_Gorilla(projectAlpha,"animals");
		return projectAlpha;
	}

	public static JavaTestProject createProjectBeta()
		throws CoreException, JavaModelException {
		projectBeta = createProject("ProjectBeta","bindir");
		addType_HelloWorld(projectBeta,"pack1");
		addType_Mammal(projectAlpha,"animals");
		addType_Gorilla(projectBeta,"animals");
		return projectBeta;
	}
	
	public static JavaTestProject createProjectGamma() 
		throws CoreException, JavaModelException {
		projectGamma = createProject("ProjectGamma", "bin");
		addType_Mammal(projectAlpha,"animals");
		addType_Ape(projectGamma, "animals");
		return projectGamma;
	}
	
	public static JavaTestProject createProjectOmega()
		throws CoreException, JavaModelException {
		projectOmega = createProject("ProjectOmega", "bin");
		addType_X(projectOmega,"alphabet");
		return projectOmega;
	}
	
	
	private static void addType_X(JavaTestProject tp, String packagename) throws CoreException {
		IPackageFragment pack = tp.createPackage(packagename);
		tp.createType(
				pack,
				"X.java",
				"public class X {\n" +
				"public int sum() { return getA() + getB(); }" +
				"public String getA() { return 1; }\n" +
				"public String getB() { return 2; }\n" +		
		"}");
	}

	private static void addType_Ape(JavaTestProject tp, String packagename) throws CoreException {
		IPackageFragment pack = tp.createPackage(packagename);
		tp.createType(
				pack,
				"Ape.java",
				"public class Ape extends Mammal {\n" +
				"\n" +
				"public int num;\n" +
				"// A Comment... \n" +
				"public boolean bool = true;\n" +
				"\n" +
				"public void eatBanana(){\n" +
				"}\n" +
				"\n" +
				"public void eatBanana(int numTimes){\n" +
				"}\n" +
				"\n" +
				"/**\n" +
				" * SomeJavaDoc\n" +
				" */\n" +
				"public void eatNuts(){\n" +
				"}");
	}

	private static void addType_HelloWorld(JavaTestProject tp,String packagename) throws CoreException {
		IPackageFragment pack = tp.createPackage(packagename);
		IType type =
			tp.createType(
				pack,
				"HelloWorld.java",
				"public class HelloWorld { "
			  +   "public static void main(String[] argv) { "
			  +     "System.err.println(\"HelloWorld\");"
			  +   "}"
			  + "}");	
	}
	
	private static void addType_Mammal(JavaTestProject tp, String packagename) throws CoreException {
		IPackageFragment pack = tp.createPackage(packagename);
		tp.createType(
				pack,
				"Mammal.java",
				"public class Mammal {\n" +
				"private String _name; \n" +
				"public String getName() { return _name; }\n" +
				"}");
	}
	
	private static void addType_Monkey(JavaTestProject tp,String packagename) throws CoreException {
		IPackageFragment pack = tp.createPackage(packagename);
		tp.createType(
			pack,
			"Monkey.java",
			"public class Monkey extends Mammal { "+
			 	"public void dance(){}\n" +
			 	"public String getName() { return \"mr. \" + super.getName();\n}" +
			"}");
	}
	
	private static void addType_Gorilla(JavaTestProject tp,String packagename) throws CoreException {
		IPackageFragment pack = tp.createPackage(packagename);
		tp.createType(
			pack,
			"Gorilla.java",
			"public class Gorilla extends Mammal { "+
			  "public static void sayhello() { "+
				"System.err.println(\"ooo oOO ooOOOo\");"+
			  "}"+
			"}");
	}
			  	
	
	
	private static JavaTestProject createProject(String projname,String bindir)
		throws CoreException, JavaModelException {
		JavaTestProject tp = new JavaTestProject(projname,bindir);
		//projectAlpha.addJar("org.junit","junit.jar");
		return tp;
	}
	
	public static void disposeProjects() throws CoreException {
		System.err.println("Disposing projects:"+projectAlpha);
		if (projectAlpha!=null) projectAlpha.dispose();
		if (projectBeta!=null) projectBeta.dispose();
		if (projectGamma!=null) projectGamma.dispose();
		if (projectWithJars!=null) projectWithJars.dispose();
	}

	public static JavaTestProject createProjectWithJars() 
		throws CoreException, JavaModelException, IOException {
		projectWithJars = createProject("ProjectWithJars","bin");
		IJavaProject javaProject = projectWithJars.getJavaProject();
		addExportedJar("org.eclipse.cme.ui.tests", "testjar.jar", javaProject);
		return projectWithJars;
	}

	
	
	/*
	 * Mostly copied from JavaTestProject - adds an exported jar to the project
	 */
	private static void addExportedJar(String plugin, String jarName, IJavaProject javaProject) throws MalformedURLException, IOException, JavaModelException {
		IFile jar = javaProject.getProject().getFile(jarName);
		Bundle bundle = Platform.getBundle(plugin);
		URL pluginURL= bundle.getEntry("/");
		URL resolved = Platform.resolve(pluginURL);
		URL jarURL= new URL(resolved, jarName);
		URL localJarURL= Platform.asLocalURL(jarURL);
		InputStream source = localJarURL.openStream();
		try{	
			jar.create(source, true, null);
			IClasspathEntry[] oldEntries= javaProject.getRawClasspath();
			IClasspathEntry[] newEntries= new IClasspathEntry[oldEntries.length + 1];
			System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
			newEntries[oldEntries.length]= JavaCore.newLibraryEntry(jar.getFullPath(), null, null, true);
			javaProject.setRawClasspath(newEntries, null);
		} catch (CoreException coreEx){
				
		}
	}

	public static void waitForJobsToComplete(IProject pro){
		Job job = new Job("Dummy Job"){
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



}
