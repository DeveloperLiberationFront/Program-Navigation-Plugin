package edu.pdx.cs.multiview.test;

import java.util.List;

import junit.framework.TestCase;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;

import edu.pdx.cs.multiview.jdt.util.IMethodReferenceFinder;

public class IMethodReferenceFinderTest extends TestCase {

	private IMethod m1,m2,refMethod;
	private ICompilationUnit cUnit;
	private IType outerType, innerType;
	
	public void setUp() throws Exception{
		
		JavaTestProject project = new JavaTestProject("MyProject");
		IPackageFragment frag = project.createPackage("edu.pdx.cs");
		
		String myClassName = "MyClass",
				innerClassName = "MyInnerClass";
		String referenceMakingMethod = "referenceMaker";
		
		String myClassContents = 
			"public class " + myClassName + "{"+
				"public void "+referenceMakingMethod+"(){" +
					"m1();" +
					"m2();" +
				"}" +
				"private void m1(){"+
					"System.out.println(\"m1\");"+
				"}"+	
				"private void m2(){"+
					"System.out.println(\"m1\");"+
				"}" +
				"private class "+innerClassName+"{" +
					"" +
				"}"+
			"}";
		
		cUnit = frag.createCompilationUnit
							(myClassName+".java",myClassContents,true,null);
		
		outerType = cUnit.getType(myClassName);
		innerType = cUnit.getType(innerClassName);
		
		String[] params = new String[0];
		refMethod = outerType.getMethod(referenceMakingMethod,params);		
		m1 = outerType.getMethod("m1",params);
		m2 = outerType.getMethod("m2",params);
		
		
	}
	
	/**
	 * I assure that method references are found within the class
	 * that they are declared.
	 * 
	 * @throws Exception
	 */
	public void testSimpleCall() throws Exception{
		
		List<IMethod> foundMethods = IMethodReferenceFinder.findMethodReference(refMethod,cUnit);
		
		assertTrue(foundMethods.contains(m1));
		assertTrue(foundMethods.contains(m2));
		
		assertEquals(2,foundMethods.size());		
	}
	
	/**
	 * I assure that methods that are referred to which are out of
	 * scope are not reported
	 *
	 */
	public void testOutOfScope(){
		
		List<IMethod> foundMethods = IMethodReferenceFinder.findMethodReference(refMethod,innerType);		
		assertEquals(0,foundMethods.size());	
	}
	
	/**
	 * I assure that a method referenced in the general java library is found.
	 *
	 */
	public void testLibraryCall(){
		List<IMethod> foundMethods = IMethodReferenceFinder.findMethodReference(m1,m1.getJavaModel());
		assertEquals(1,foundMethods.size());
		
		assertTrue(foundMethods.get(0).getElementName().contains("println"));
	}
}
