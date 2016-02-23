package edu.pdx.cs.multiview.test;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;


public class JDTTestingUtils extends TestCase{

	public void textX() throws CoreException{
		getWritableJavaIO();
	}
	
	private static String rtJarString = "rt.jar",
							javaIOString = "java.io";
	
	public static IPackageFragment getReadOnlyJavaIO(IJavaElement something) 
			throws JavaModelException {
		IPackageFragmentRoot rtJar = null;
		for(IPackageFragmentRoot root : something.getJavaProject().getPackageFragmentRoots()){
			if(root.getElementName().equals(rtJarString))
				rtJar = root;
		}
		
		assertTrue(rtJar!=null);
		assertTrue(rtJar.exists());
		
		IPackageFragment javaIO = rtJar.getPackageFragment(javaIOString);
		
		assertTrue(javaIO.exists());
		return javaIO;
	}

	public static IPackageFragment getWritableJavaIO() 
					throws CoreException{

		JavaTestProject project = new JavaTestProject();
		IJavaProject destination = project.getJavaProject();
		
		IPackageFragment destFrag = project.createPackage(javaIOString);
		assertFalse(destFrag.isReadOnly());
		assertTrue(destFrag.exists());
		
		IPackageFragment source = getReadOnlyJavaIO(destination);
		
		
		String contents;
		for(IClassFile clazz : source.getClassFiles()){
			contents = clazz.getSource();
			assertTrue(contents!=null);//if this fails, you probably have to attach the source
			destFrag.createCompilationUnit(clazz.getElementName().replace(".class",".java"),
						contents,true,new NullProgressMonitor());
		}
		
		
		return destFrag;
	}
}
