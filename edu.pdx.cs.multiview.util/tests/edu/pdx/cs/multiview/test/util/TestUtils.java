package edu.pdx.cs.multiview.test.util;

import junit.framework.TestCase;


import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Test our test utils.
 * 
 * @author pq
 *
 */
public class TestUtils extends TestCase {

	/** Test projects */
	IJavaProject _projectAlpha;
	//IJavaProject _projectBeta;
	//IJavaProject _projectGamma;

	
	/*
	 * @see TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_projectAlpha = Utils.createProjectAlpha().getJavaProject();
		//_projectBeta  = Utils.createProjectBeta().getJavaProject();
		//_projectGamma = Utils.createProjectGamma().getJavaProject();
	}
	
	/**
	 * Test assumptions about our alpha test env.
	 * @throws JavaModelException
	 */
	public void testProjectAlphaEnv1() throws JavaModelException {
		IType monkey = _projectAlpha.findType("animals.Monkey");
		assertTrue(monkey.exists());
		IMethod dance = monkey.getMethod("dance", new String[]{});
		assertTrue(dance.exists());
		IMethod getName = monkey.getMethod("getName", new String[]{});
		assertTrue(getName.exists());
		IType mammal = _projectAlpha.findType("animals.Mammal");
		assertTrue(mammal.exists());
		IMethod getName2 = mammal.getMethod("getName", new String[]{});
		assertTrue(getName2.exists());
		IField name = mammal.getField("_name");
		assertTrue(name.exists());
	}
	
	
	
	
}
