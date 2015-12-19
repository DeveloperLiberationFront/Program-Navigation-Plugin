package edu.pdx.cs.multiview.jdt.delta;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.MalformedTreeException;

import edu.pdx.cs.multiview.jdt.util.JDTUtils;
import edu.pdx.cs.multiview.test.JavaTestProject;
import edu.pdx.cs.multiview.util.Debug;

public class JDTUtilsTest extends TestCase {

	/** A test source buffer */
	private static final String TEST_CLASS_SRC = "public class TestClass {\n" +
				"\tvoid m(){\n\t\tboolean value;\n\t}\n" +
				"\tvoid n(){}\n" +
				"\tint x;\n}";
	
	public void testGetMethodBody() throws CoreException, MalformedTreeException, BadLocationException {
		JavaTestProject proj = new JavaTestProject(getClass().getName());
		IPackageFragment pkg = proj.createPackage("test");
		IType type = proj.createType(pkg, "TestClass.java", TEST_CLASS_SRC);
		IMethod m = type.getMethod("m", new String[]{});
		//String body = JDTUtils.getBody(m);
		//assertEquals("{boolean value;}",body);
		
		String bad = "{\n\t\t   //blah\n\n//more text\n }";
		
		String fullSrc = type.getSource();
		String src = m.getSource();
		String formatted = JDTUtils.format(bad, type, 0, CodeFormatter.K_CLASS_BODY_DECLARATIONS, "\n");
		Debug.trace(formatted);
		
		int indent = JDTUtils.inferIndentLevel(m);
		System.err.println(indent);
	
		//insert a new method with the right formatting
		String newMethod = "void newMethod(){\n\tint x;\n}";
		IMethod sibling = type.getMethods()[0];
		int siblingIndent = JDTUtils.inferIndentLevel(sibling);
		String newMethodFormatted = JDTUtils.format(newMethod, type, siblingIndent);
		type.createMethod(newMethodFormatted, sibling, true, null);
		
		System.err.println(type.getSource());
		String body = "{\nint foo;\n\n\t\t\tint y = 13;\n\t}";
		String badM = "void badformat()" + body;
		type.createMethod(badM, sibling, true, null);
		System.err.println(type.getSource());
		String goodM = JDTUtils.format("void goodformat()" + body, type, siblingIndent);
		type.createMethod(goodM, sibling, true, null);
		System.err.println(type.getSource());
	}
	
	
	
}
