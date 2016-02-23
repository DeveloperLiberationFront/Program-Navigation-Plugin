package edu.pdx.cs.multiview.jdt.delta;

import java.util.List;


public interface IMemberDelta {
	
	/**
	 * The type of delta.
	 */
	public static enum DeltaKind {
		UNCHANGED,
		ADDED,
		REMOVED,
		MODIFIED, 
		CHILDREN;
	}
	
	
	/** @return the handle identifier of the underlying member */
	IMemberInfo getMemberInfo();

	/** @return the handle identifier of the underlying member */
	String getHandleId();
	
	
	/** @return the kind of delta */
	DeltaKind getKind();

	/** @return	all affected children, if any */
	List<IMemberDelta> getAffectedChildren();
}
