package edu.pdx.cs.multiview.jdt.util;

//copied from: package org.eclipse.jdt.internal.ui.search;


import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

public class PrettySignature {

//	public static String getSignature(IJavaElement element) {
//		if (element == null)
//			return null;
//		else
//			switch (element.getElementType()) {
//				case IJavaElement.METHOD:
//					return getMethodSignature((IMethod)element);
//				case IJavaElement.TYPE:
//					return JavaModelUtil.getFullyQualifiedName((IType)element);
//				default:
//					return element.getElementName();
//			}
//	}
	

//	/**
//	 * Format: [TypeName].[methodName][(]<paramTypes>[)]
//	 * 
//	 * @param method
//	 * @return
//	 */
//	public static String getMethodSignature(IMethod method) {
//		StringBuffer buffer= new StringBuffer();
//		buffer.append(JavaModelUtil.getFullyQualifiedName(method.getDeclaringType()));
//		boolean isConstructor= method.getElementName().equals(method.getDeclaringType().getElementName());
//		if (!isConstructor) {
//			buffer.append('.');
//		}
//		buffer.append(getUnqualifiedMethodSignature(method, !isConstructor));
//		
//		return buffer.toString();
//	}

	public static String getUnqualifiedTypeSignature(IType type) {
		return type.getElementName();
	}
	
	public static String getUnqualifiedMethodSignature(IMethod method, boolean includeName) {
		StringBuffer buffer= new StringBuffer();
		if (includeName) {
			buffer.append(method.getElementName());
		}
		buffer.append('(');
		
		String[] types= method.getParameterTypes();
		if (types.length > 0)
			buffer.append(Signature.toString(types[0]));
		for (int i= 1; i < types.length; i++) {
			buffer.append(", "); //$NON-NLS-1$
			buffer.append(Signature.toString(types[i]));
		}
		
		buffer.append(')');
		
		return buffer.toString();
	}

	public static String getUnqualifiedMethodSignature(IMethod method) {
		return getUnqualifiedMethodSignature(method, true);
	}	
	
	/**
	 * 
	 * Ex: public static void foo(int x, String z)
	 * 
	 * @param   method
	 * @return  a String that represents the signature part of a method for use in 
	 * 			building a compilable source String
	 * @throws  JavaModelException
	 */
	public static String getCompilableMethodSignature(IMethod method) throws JavaModelException {
		StringBuffer buffer= new StringBuffer();
		int flags = method.getFlags();
		buffer.append(getVisibilityString(flags));
		buffer.append(getModifierString(flags));
		buffer.append(getReturnTypeString(method)).append(" ");
		buffer.append(getUnqualifiedMethodSignatureWithParamNames(method));
		return buffer.toString();
	}


	/**
	 * static, final, synchronized,...
	 * @param flags
	 * @return
	 */
	private static String getModifierString(int flags) {
		StringBuffer buf = new StringBuffer();
		if (Flags.isStatic(flags))
			buf.append("static ");
		if (Flags.isFinal(flags))
			buf.append("final ");
		if (Flags.isSynchronized(flags))
			buf.append("synchronized ");
		return buf.toString();
	}


	private static String getUnqualifiedMethodSignatureWithParamNames(IMethod method) throws JavaModelException {
		StringBuffer buffer= new StringBuffer();
		
		buffer.append(method.getElementName());
		
		buffer.append('(');
		
		String[] types= method.getParameterTypes();
		String[] names= method.getParameterNames();
		
		if (types.length > 0) {
			buffer.append(Signature.toString(types[0]));
			buffer.append(" ").append(names[0]);
		}
		
		for (int i= 1; i < types.length; i++) {
			buffer.append(", "); //$NON-NLS-1$
			buffer.append(Signature.toString(types[i]));
			buffer.append(" ").append(names[i]);
		}
		
		buffer.append(')');
		
		return buffer.toString();
	}


	private static String getReturnTypeString(IMethod method) throws IllegalArgumentException, JavaModelException {
		return Signature.toString(method.getReturnType());
	}


	private static String getVisibilityString(int flags) {
		if (Flags.isPrivate(flags))
			return "private ";
		if (Flags.isProtected(flags))
			return "protected ";
		if (Flags.isPublic(flags))
			return "public ";
		
		return ""; //pkg
	}		
	
}

