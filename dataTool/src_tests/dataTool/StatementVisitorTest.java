package dataTool;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import dataTool.StatementVisitor;
import edu.pdx.cs.multiview.test.JavaTestProject;

/**
 * Makes sure the statement visitor behaves as expected
 * 
 * @author emerson
 */
public class StatementVisitorTest extends TestCase{

	private JavaTestProject project;
	
	public void setUp() throws Exception{
		project = new JavaTestProject();
		project.copyToSourceDir (JavaTestProject.JAVA_IO_LOC);
	}
	
	public void testBasic() throws JavaModelException, CoreException{
		IType type = project.getType("CharArrayReader", "java.io");
		
		StatementVisitor visitor = parse(type);
		
		String statement = "this.buf=buf;\n";
		
		//the beginning of a statement
		assertTrue(visitor.statementAt(981,true).toString().equals(statement));
		
		//a little whitespace out front
		assertTrue(visitor.statementAt(980,true).toString().equals(statement));
		
		//the middle of the statement
		assertTrue(visitor.statementAt(983,true).toString().equals(statement));		
	}
	
	public void testLastStatement() throws JavaModelException, CoreException{
		
		IType type = project.getType("CharArrayReader", "java.io");
		
		StatementVisitor visitor = parse(type);
		
		String statement = "this.count=buf.length;\n";
		
		//the middle of the statement
		assertTrue(visitor.statementAt(1022,true).toString().equals(statement));		
	}
	

	private StatementVisitor parse(IType type) throws JavaModelException {
		String source = type.getCompilationUnit().getSource();
		
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(source.toCharArray());
		CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);
		
		StatementVisitor visitor = new StatementVisitor(source);			
		astRoot.accept(visitor);
		
		return visitor;
	}
}
