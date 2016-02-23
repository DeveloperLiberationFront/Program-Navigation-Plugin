package edu.pdx.cs.multiview.jdt.delta;

import java.util.ArrayList;
import java.util.List;


public class MemberDelta implements IMemberDelta {


	/** The kind of delta (ADDED, MODIFIED or REMOVED) */
	private DeltaKind _kind;
	
	/** The member info underlying the delta */
	private IMemberInfo _memberInfo;
	
	 private List<IMemberDelta> children = new ArrayList<IMemberDelta>();
	
	/**
	 * Create an instance.
	 * @param handleId 
	 */
	public MemberDelta(IMemberInfo memberInfo, DeltaKind kind) {
		_memberInfo = memberInfo; 
		_kind       = kind;
	}
	
	/** @return the handle identifier of the underlying member */
	public IMemberInfo getMemberInfo() {
		return _memberInfo;
	}

	/** @return the handle identifier of the underlying member */
	public String getHandleId() {
		return _memberInfo.getHandle();
	}
	
	
	/** @return the kind of delta */
	public DeltaKind getKind() {
		return _kind;
	}

	
	public static void main(String[] args) {
		System.out.print(DeltaKind.ADDED);
	}

	public List<IMemberDelta> getAffectedChildren() {		
		return children;
	}
	
	public void addChild(IMemberDelta md){
		children.add(md);
	}

}
