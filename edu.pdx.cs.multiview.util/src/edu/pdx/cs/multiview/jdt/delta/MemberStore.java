package edu.pdx.cs.multiview.jdt.delta;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;

/**
 * 
 * A class for caching members to which delta listeners can be 
 * associated.
 * 
 * @author pq
 *
 */
public class MemberStore {

	/** The map of parent keys to sets of cached members */
	Map<String, IMemberSet> _map = new HashMap<String, IMemberSet>();
	
	/**
	 * Add a listener to this member
	 * @param member
	 * @param listener
	 */
	public void addListener(IMember member, MemberModel.IModelListener listener) {
		//get the entry (or create a new one)
		IMemberInfo entry = get(member);
		entry.addListener(listener);
		//return info?
	}

	/**
	 * Remove the given listener from this member.
	 * @param member
	 * @param listener
	 */
	public void removeListener(IMember member, MemberModel.IModelListener listener) {
		IMemberInfo entry = get(member);
		entry.removeListener(listener);
	}

	
	/**
	 * Get the entry for this member.
	 * @param member
	 * @return
	 * N.B. if entry does not exist, it is created!
	 */
	public IMemberInfo get(IMember member) {
		String key = getParentKey(member);
		IMemberSet children = _map.get(key);
		if (children == null) {
			children = new MemberSet();
			_map.put(key,children);
		}
		IMemberInfo info = children.getInfo(member); 
		if (info == null) {
			info = MemberInfo.createInfo(member);
			children.add(info);
		}
		return info;
	}

	/**
	 * Get the entry for this (parent) member (by handle).
	 * @param memberHandle
	 * @return
	 */
	public IMemberInfo get(String memberHandle) {
		return get((IMember)JavaCore.create(memberHandle));
	}	
	
	/**
	 * Get the handle identifire of the parent element that contains this
	 * member in the Java model.
	 * @param member
	 * @return
	 */
	private String getParentKey(IMember member) {
		IType parent = getParent(member);
		return parent.getHandleIdentifier();
	}
	
	
	/**
	 * Get the parent type to this member.
	 * @param member
	 * @return
	 * TODO: put in util class
	 */
	private static IType getParent(IMember member) {
		if (member instanceof IType) {
			if (member.getDeclaringType() == null)
				return (IType)member;
		}
		return getParent(member.getDeclaringType());
	}

	/**
	 * Get the child elements who might be affected by a change to this 
	 * type member.
	 * @param type
	 * @return
	 */
	public IMemberSet getChildren(IMember type) {
		String key = type.getHandleIdentifier();
		return _map.get(key);
	}

	/**
	 * Put the member at this handle into the store.
	 * @param handleId
	 * @param info
	 */
	public void put(String handleId, IMemberInfo info) {
		String key = getParentKey((IMember)JavaCore.create(handleId));
		IMemberSet memberSet = _map.get(key);
		if (memberSet == null) {
			memberSet = new MemberSet();
			_map.put(key,memberSet);
		}
		memberSet.add(handleId, info);
	}

	/**
	 * Remove the member at this handle from the store.
	 * @param handleId
	 * @return 
	 */
	public IMemberInfo remove(String handleId) {
		String key = getParentKey((IMember)JavaCore.create(handleId));
		IMemberSet memberSet = _map.get(key);
		if (memberSet != null)
			return memberSet.remove(handleId);
		return null;
	}

	/**
	 * Checks to see if the given member is present.
	 * @param member
	 * @return
	 */
	public boolean contains(IMember member) {
		String key = getParentKey(member);
		IMemberSet children = _map.get(key);
		if (children == null) {
			return false;
		}
		return children.getInfo(member) != null; 
	}
	
	@Override
	public String toString(){
		return _map.toString();
	}
	
}
