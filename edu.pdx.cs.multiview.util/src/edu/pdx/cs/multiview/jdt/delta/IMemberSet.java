package edu.pdx.cs.multiview.jdt.delta;

import java.util.Collection;

import org.eclipse.jdt.core.IMember;

/**
 * A container for associations of IMember handles to 
 * IMemberInfo objects.
 * 
 * @author pq
 *
 */
public interface IMemberSet {

	/**
	 * Get the member info associated with this member.
	 * @param member
	 * @return the associated member info
	 */
	IMemberInfo getInfo(IMember member);

	/** Add this member info to the set.
	 * @param info
	 */
	void add(IMemberInfo info);

	/**
	 * Add this info at this handleId
	 * @param handleId
	 * @param info
	 * FIXME: should this really be exposed API?
	 */
	void add(String handleId, IMemberInfo info);

	/**
	 * @return a view of the members as a collection
	 */
	Collection<? extends IMemberInfo> getMembers();

	/**
	 * Remove the member identified by this handle from the set.
	 * @param handleIdentifier
	 */
	IMemberInfo remove(String handleIdentifier);

}
