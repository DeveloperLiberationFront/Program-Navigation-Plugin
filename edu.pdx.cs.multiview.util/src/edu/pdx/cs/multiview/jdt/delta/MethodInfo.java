package edu.pdx.cs.multiview.jdt.delta;

import org.eclipse.jdt.core.IMethod;

public class MethodInfo extends MemberInfo {

	/**
	 * Create an instance.
	 * @param method
	 */
	protected MethodInfo(IMethod method) {
		super(method);	
	}

}
