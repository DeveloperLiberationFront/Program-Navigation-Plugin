package edu.pdx.cs.multiview.test;

import junit.framework.TestCase;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

public class SourceCopyingTests extends TestCase {

	public void testCopySource() throws Exception {
		JavaTestProject project = new JavaTestProject("foo");
		project.copyToSourceDir(JavaTestProject.TEST_DATA);
		IType type = project.getJavaProject().findType("foo.Bar");
		assertNotNull(type);
		assertTrue(type.exists());
	}

	public void testAddToCopiedSource() throws Exception {
		JavaTestProject project = new JavaTestProject("foo");
		project.copyToSourceDir(JavaTestProject.TEST_DATA);
		IType type = project.getJavaProject().findType("foo.Bar");
		assertNotNull(type);
		assertTrue(type.exists());
		type.createField("String _name;", null, true, null);
		assertTrue(type.getField("_name").exists());
	}
	
	
	public void testCopyJavaIOSource() throws Exception {
		JavaTestProject project = new JavaTestProject("java.io");
		project.copyToSourceDir(JavaTestProject.JAVA_IO_LOC);
		IType writer = project.getJavaProject().findType("java.io.Writer");
		assertNotNull(writer);
		assertTrue(writer.exists());
		IMethod method = writer.getMethod("flush", new String[]{});
		assertTrue(method.exists());
	}
	
	
	
	
	
	
}
