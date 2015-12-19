package edu.pdx.cs.multiview.jdt.util;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.text.Position;

/**
 * I unify MethodInvoctions and FieldAccesses, as well as 
 * their bindings
 * 
 * @author emerson
 */
public abstract class MemberReference{

	private final ASTNode reference;
	private Position p;
	
	public MemberReference(ASTNode n){
		this.reference = n;
		this.p = new Position(n.getStartPosition(),n.getLength());
	}
	
	protected MemberReference(MemberReference mr){
		this(mr.reference);
	}
	
	public static MemberReference with(MethodInvocation m){		
		return bindOrNull(new MethodBinding(m));
	}

	public static MemberReference with(ClassInstanceCreation m){
		return bindOrNull(new MethodBinding(m));
	}
	
	private static MemberReference bindOrNull(MemberReference mb) {
		if(mb.getBinding()==null)
			return null;
		return mb;
	}
	
	public static MemberReference with(SimpleName name, IVariableBinding binding){
		return bindOrNull(new FieldBinding(name,binding));
	}
	
	public boolean equals(Object o){
		
		if(!(o instanceof MemberReference))
			return false;
		
		return getBinding().equals(((MemberReference)o).getBinding());
	}
	
	public int hashCode(){
		return 3*getBinding().hashCode();
	}
	
	public abstract IBinding getBinding();
	
	public Position getPosition(){
		return p;
	}
	
	static class MethodBinding extends MemberReference{

		private IMethodBinding methodBinding;

		public MethodBinding(MethodInvocation m) {
			this(m.getName(),m.resolveMethodBinding());
		}
		
		public MethodBinding(ClassInstanceCreation m){
			this(m.getType(),m.resolveConstructorBinding());
		}
		
		private MethodBinding(ASTNode name, IMethodBinding binding){
			super(name);
			methodBinding = binding;
		}

		@Override
		public IMethodBinding getBinding() {
			return methodBinding;
		}
		
		@Override
		public ITypeBinding referencedClass() {
			return methodBinding.getDeclaringClass();
		}
		
		public String toString(){
			
			String args = "";
			if(getBinding().getParameterTypes().length > 0){
				args = "...";
			}
			return getBinding().getName() + "(" + args + ")";
		}
	}
	
	static class FieldBinding extends MemberReference{

		private IVariableBinding fieldBinding;

		public FieldBinding(SimpleName sn, IVariableBinding binding) {
			super(sn);
			fieldBinding = binding;
		}		
		
		@Override
		public IVariableBinding getBinding() {
			return fieldBinding;
		}

		@Override
		public ITypeBinding referencedClass() {
			return fieldBinding.getDeclaringClass();
		}
		
		public String toString(){
			return getBinding().getName();
		}
	}

	public abstract ITypeBinding referencedClass();
	
	public TypeDeclaration declaringClass(){
		ASTNode current = reference;
		do{
			
			if(current instanceof TypeDeclaration){
				return (TypeDeclaration) current;
			}
			
		}while((current = current.getParent()) != null);
		
		return null;
	}

	public String classToString() {
		String name = referencedClass().getName();
		return name.contains("<") ? name.substring(0,name.indexOf('<')) : name;
	}
	
	public boolean refersToReadOnlyClass(){
		return referencedClass().getJavaElement().isReadOnly();
	}
	
	public boolean isThisReference(){
		return declaringClass().resolveBinding().equals(referencedClass());
	}
}
