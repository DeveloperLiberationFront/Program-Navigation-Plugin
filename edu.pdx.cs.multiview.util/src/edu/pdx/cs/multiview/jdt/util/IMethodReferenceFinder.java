package edu.pdx.cs.multiview.jdt.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import edu.pdx.cs.multiview.util.Debug;

public class IMethodReferenceFinder {
	//TODO: test this class
	/**
	 * @param source	a Method
	 * @param scope		
	 * 
	 * @return			all methods referenced from the argument within scope
	 */
	public static List<IMethod> findMethodReference(IMethod source, IJavaElement scope){
		CompilationUnit unit = 
			JDTUtils.parseCompilationUnit(source.getCompilationUnit(),true);
		
		CallFindingVisitor visitor = new CallFindingVisitor(source);
		unit.accept(visitor);
		
		return visitor.getInvocationsIn(scope);
	}
	
	private static class CallFindingVisitor extends ASTVisitor{
		
		private Vector<MethodInvocation> invocations = 
			new Vector<MethodInvocation>();
		private IMethod source;
		
		public CallFindingVisitor(IMethod source) {
			this.source = source;
		}

		@Override
		public boolean visit(MethodInvocation invocation){
		
			if(isParentedByMethod(invocation))
				invocations.add(invocation);			
			return true;
		}	
		
		/**		
		 * @return	whether the argument is parented by the method
		 * we are looking in
		 */
		private boolean isParentedByMethod(ASTNode node){
			
			if(node==null || node instanceof CompilationUnit)
				return false;
			
			if(node instanceof MethodDeclaration){
				MethodDeclaration decl = (MethodDeclaration)node;
				if(decl.resolveBinding().getJavaElement().equals(source))
					return true;
			}
			
			return isParentedByMethod(node.getParent());
		}
		
		public List<IMethod> getInvocationsIn(IJavaElement scope){
			
			List<IMethod> methods = new ArrayList<IMethod>();
			
			for(MethodInvocation invocation : invocations){
				
				IMethodBinding binding = invocation.resolveMethodBinding();
				if(binding!=null){
					IMethod callee = (IMethod)binding.getJavaElement();
					if(!methods.contains(callee) && JDTUtils.isParentOf(callee,scope))
						methods.add(callee);
				}else{
					Debug.error("Unable to resolve binding for " + invocation);
				}
				
			}
			
			return methods;
		}
	}
}
