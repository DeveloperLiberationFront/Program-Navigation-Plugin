package edu.pdx.cs.multiview.jdt.delta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IMember;

/**
 * A set of MemberInfo objects.
 * 
 * @author pq
 *
 */
public class MemberSet implements IMemberSet {

	/** The map of member handles to their associated member info */
	Map<String,IMemberInfo> _members = new HashMap<String,IMemberInfo>();

	/**
	 * Return the info object associated with this IMember.
	 * @param member - the member
	 * @return the info object
	 */
	public IMemberInfo getInfo(IMember member) {
		String key = member.getHandleIdentifier();
		return _members.get(key);
	}
	
	/* (non-Javadoc)
	 * @see edu.pdx.cs.multiview.jdt.delta.IMemberSet#getMembers()
	 */
	public Collection<? extends IMemberInfo> getMembers() {
		Collection<? extends IMemberInfo> members = _members.values();
		if (members == null)
			return new ArrayList<IMemberInfo>();
		return members;
	}
	
	/* (non-Javadoc)
	 * @see edu.pdx.cs.multiview.jdt.delta.IMemberSet#add(java.lang.String, edu.pdx.cs.multiview.jdt.delta.IMemberInfo)
	 */
	public void add(String handleId, IMemberInfo info) {
		_members.put(handleId,info);
	}

	/* (non-Javadoc)
	 * @see edu.pdx.cs.multiview.jdt.delta.IMemberSet#add(edu.pdx.cs.multiview.jdt.delta.IMemberInfo)
	 */
	public void add(IMemberInfo info) {
		add(info.getHandle(), info);
	}
	
	/* (non-Javadoc)
	 * @see edu.pdx.cs.multiview.jdt.delta.IMemberSet#remove(java.lang.String)
	 */
	public IMemberInfo remove(String handleIdentifier) {
		return _members.remove(handleIdentifier);
	}
	
	@Override
	public String toString(){
		return _members.toString();
	}
}
