package dataTool;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import dataTool.Visitor;
import edu.pdx.cs.multiview.test.JavaTestProject;

/**
 * Makes sure the statement visitor behaves as expected
 * 
 * @author Chris
 */
public class VisitorTest extends TestCase{

	private JavaTestProject project;
	private Visitor visitor;
		
	public void setUp() throws Exception{
		//project = new JavaTestProject();
		//project.copyToSourceDir (JavaTestProject.JAVA_IO_LOC);
	}
	
	public void testParseDataEnhancedFor() {
		String enhancedForTestStr = "public class Test {" +
						 "public void func() {\n" + 
						 "	for(int i = 0; i < 100; i++) {\n" +
						 "		System.out.println(i);\n" +
						 "		int test = i*5\n" +
						 "	}\n" +
						 "}}";
		
		visitor = new Visitor(enhancedForTestStr);
		visitor.parseData();
		System.out.println(visitor.getData().size());
	}
	
}
