package edu.pdx.cs.multiview.jdt.delta;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import edu.pdx.cs.multiview.jdt.delta.IMemberDelta.DeltaKind;
import edu.pdx.cs.multiview.util.Debug;

public class TypeInfo extends MemberInfo{

	protected TypeInfo(IType member) {
		super(member);
	}
	
	protected Object memoize(IMember member) {
		
		IType type = (IType)member;
		
		Set<String> memo = null;
		try {
			memo = new HashSet<String>(type.getChildren().length);
			for(IJavaElement child : type.getChildren())
				memo.add(child.getHandleIdentifier());
			
		} catch (JavaModelException e) {
			Debug.report(e);
		}
		
		return memo;
	}
	
	public MemberDelta createModifiedDelta(IMemberInfo nu) {
		
		MemberDelta md = new MemberDelta(this, DeltaKind.MODIFIED);
		
		Set<String> newMemo = (Set<String>)nu.getMemo();
		Set<String> oldMemo = (Set<String>)this.getMemo();
		
		for(String handle : newMemo)
			if(!oldMemo.contains(handle))
				md.addChild(new MemberDelta(createInfo(handle),DeltaKind.ADDED));
		
		for(String handle : oldMemo)
			if(!newMemo.contains(handle))
				md.addChild(new MemberDelta(createInfo(handle),DeltaKind.REMOVED));
		
		return md;
	}
}
