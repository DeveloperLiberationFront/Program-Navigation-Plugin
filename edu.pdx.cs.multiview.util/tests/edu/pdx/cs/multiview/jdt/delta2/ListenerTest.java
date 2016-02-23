package edu.pdx.cs.multiview.jdt.delta2;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.compiler.InvalidInputException;

import edu.pdx.cs.multiview.jdt.delta.IMemberDelta;
import edu.pdx.cs.multiview.jdt.delta.MemberModel;
import edu.pdx.cs.multiview.jdt.delta.IMemberDelta.DeltaKind;
import edu.pdx.cs.multiview.jdt.delta.MemberModel.IModelListener;
import edu.pdx.cs.multiview.jdt.util.JDTUtils;
import edu.pdx.cs.multiview.test.JavaTestProject;

/**
 * I make sure that our java model listener is working
 * 
 * @author emerson
 */
public class ListenerTest extends TestCase{

	private JavaTestProject project;
	
	public void setUp() throws Exception{
		project = new JavaTestProject();
		project.copyToSourceDir (JavaTestProject.JAVA_IO_LOC);
	}
	
	/**
	 * Assures notification when a member is changed
	 * 
	 * @throws CoreException 
	 * @throws InvalidInputException 
	 */
	public void testMemberChanged() throws Exception{
		
		IType reader = project.getType("Reader","java.io");
		IMethod method = reader.getMethod("ready",new String[0]);
		
		Listener listener = new Listener();
		listener.reset();
		
		//preconditions
		assertNotNull(reader);
		assertNotNull(method);
		
		MemberModel.addListener(method,listener);
		
		JDTUtils.changeBody(method,"System.out.println()");
		
		assertEquals(DeltaKind.MODIFIED,listener.first().getKind());
		listener.reset();
		
		MemberModel.removeListener(method,listener);
		
		JDTUtils.changeBody(method,"System.err.println()");
		
		assertEquals(0,listener.eventCount());
	}
	
	/**
	 * Assures notification when a member is removed
	 */
	public void testMemberRemoved() throws Exception{
		
		IType writer = project.getType("Writer","java.io");
		
		Listener listener = new Listener();
		listener.reset();
		
		IMethod flush = writer.getMethod("flush",new String[0]);
		IField lock = writer.getField("lock");
		
		//preconditions
		assertNotNull(writer);
		assertTrue(writer.exists());
		assertNotNull(flush);
		assertTrue(flush.exists());
		assertNotNull(lock);
		assertTrue(lock.exists());
		
		MemberModel.addListener(writer,listener);
		
		flush.delete(true,null);
		JavaTestProject.waitForJobsToComplete(project.getProject());
		
		assertEquals(DeltaKind.MODIFIED,listener.first().getKind());
		IMemberDelta removeDelta = listener.first().getAffectedChildren().get(0);
		assertEquals(DeltaKind.REMOVED,removeDelta.getKind());
		listener.reset();
		
		lock.delete(true,null);
		JavaTestProject.waitForJobsToComplete(project.getProject());
		
		assertEquals(DeltaKind.MODIFIED,listener.first().getKind());
		removeDelta = listener.first().getAffectedChildren().get(0);
		assertEquals(DeltaKind.REMOVED,removeDelta.getKind());
		listener.reset();
	}
	
	/**
	 * Assures notification when a member is added
	 */
	public void testMemberAdded() throws Exception{
		
		IType writer = project.getType("Writer","java.io");
		
		Listener listener = new Listener();
		listener.reset();
		
		//preconditions
		assertNotNull(writer);
		
		MemberModel.addListener(writer,listener);
		
		writer.createMethod("void m1(){}",null,true,null);		
		assertEquals(DeltaKind.MODIFIED,listener.first().getKind());
		IMemberDelta addDelta = listener.first().getAffectedChildren().get(0);
		assertEquals(DeltaKind.ADDED,addDelta.getKind());
		listener.reset();
		
		writer.createField("private String x;",null,true,null);
		assertEquals(DeltaKind.MODIFIED,listener.first().getKind());
		addDelta = listener.first().getAffectedChildren().get(0);
		assertEquals(DeltaKind.ADDED,addDelta.getKind());
		listener.reset();
		
		writer.createInitializer("{System.out.println();}",null,null);
		assertEquals(DeltaKind.MODIFIED,listener.first().getKind());
		addDelta = listener.first().getAffectedChildren().get(0);
		assertEquals(DeltaKind.ADDED,addDelta.getKind());
		listener.reset();
		
		writer.createType("private class MyType(){}",null,true,null);
		assertEquals(DeltaKind.MODIFIED,listener.first().getKind());
		addDelta = listener.first().getAffectedChildren().get(0);
		assertEquals(DeltaKind.ADDED,addDelta.getKind());
		listener.reset();
	}
	
	/**
	 * Assures notification when a class is added
	 */
	public void testTypeAdded() throws Exception{
		
		IPackageFragment frag = project.createPackage("edu");
		
		Listener listener = new Listener();
		listener.reset();
		
		//preconditions
		assertNotNull(frag);
		
		MemberModel.addListenerForTypeChange(listener);
		
		ICompilationUnit foo = frag.createCompilationUnit("Foo.java","public class Foo{}",true,null);
		
		JavaTestProject.waitForJobsToComplete(project.getProject());
		
		assertEquals(DeltaKind.ADDED,listener.first().getKind());
		listener.reset();
		
		JavaTestProject.waitForJobsToComplete(project.getProject());
		
		foo.findPrimaryType().createType("private class Bar(){}",null,true,null);
		assertEquals(DeltaKind.ADDED,listener.first().getKind());
		listener.reset();
	}
	
	/**
	 * Assures notification when a class is removed
	 */
	public void testTypeRemoved() throws Exception{
		
		IType osc = project.getType("ObjectStreamClass","java.io");
		IType entryFuture = osc.getType("EntryFuture");
		
		assertNotNull(osc);
		assertNotNull(entryFuture);
		
		Listener listener = new Listener();
		listener.reset();
		
		MemberModel.addListenerForTypeChange(listener);
		
		entryFuture.delete(true,null);
		assertEquals(DeltaKind.REMOVED,listener.first().getKind());
		listener.reset();
		
		osc.delete(true,null);
		assertEquals(DeltaKind.REMOVED,listener.first().getKind());
		listener.reset();
	}
	
	private class Listener implements IModelListener{

		private List<IMemberDelta> deltas = 
					new LinkedList<IMemberDelta>();
		
		public void notify(IMemberDelta delta) {
			deltas.add(delta);
		}
		
		public void reset(){
			deltas.clear();
		}
		
		public int eventCount(){
			return deltas.size();
		}
		
		public IMemberDelta first(){
			if(deltas.size()<1)
				throw new IllegalArgumentException("No events recieved!");
			return deltas.get(0);
		}
		
	}
}
