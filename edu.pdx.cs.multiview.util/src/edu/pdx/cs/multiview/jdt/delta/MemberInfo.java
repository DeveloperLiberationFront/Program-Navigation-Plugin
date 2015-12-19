package edu.pdx.cs.multiview.jdt.delta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import edu.pdx.cs.multiview.jdt.delta.IMemberDelta.DeltaKind;
import edu.pdx.cs.multiview.jdt.delta.MemberModel.IModelListener;
import edu.pdx.cs.multiview.util.Debug;

/**
 * A cache for member info for use in dispatching Member change events.
 * @author pq
 */
public abstract class MemberInfo implements IMemberInfo {

	/** The cache for memoized members */
	private static final Map<String /*handleId*/, MemberInfo> CACHE = new HashMap<String /*handle*/, MemberInfo>();

	
	
	//FIXME: what should this type be?  and generic?
	/** An object that memoizes the state of this member */
	private Object _memo;

	/** The member's handle id */
	private String _handle;



	private Set<IModelListener> _listeners = new HashSet<MemberModel.IModelListener>();
	
	/**
	 * Create an instance.
	 * @param member
	 */
	protected MemberInfo(IMember member) {
		_memo   = memoize(member);
		if(_memo==null)
			Debug.trace("Can't memoize " + member.getElementName() + " (it may have been deleted)");
		_handle = member.getHandleIdentifier(); 
	}

	/* (non-Javadoc)
	 * @see edu.pdx.cs.multiview.jdt.delta.IMemberInfo#getHandle()
	 */
	public String getHandle() {
		return _handle;
	}
	
	/* (non-Javadoc)
	 * @see edu.pdx.cs.multiview.jdt.delta.IMemberInfo#getMemo()
	 */
	public Object getMemo() {
		return _memo;
	}
	
	/* (non-Javadoc)
	 * @see edu.pdx.cs.multiview.jdt.delta.IMemberInfo#compare(edu.pdx.cs.multiview.jdt.delta.IMemberInfo)
	 */
	public IMemberDelta compare(IMemberInfo other) {
		if (other == null || other.getMemo() == null)
			return createRemovedDelta(this);
		if(_memo==null)
			return createAddedDelta(other);//maybe???
		//TODO: emerson and phil - getting a null pointer here - memo is null?
		if (_memo.equals(other.getMemo()))
			return createUnchangedDelta(this);
		return createModifiedDelta(other);
	}
	
	/**
	 * Memoize the state of this member.  The default implementation is based on
	 * the associated source, stripped of all comments and whitespace.
	 * @param member - the member to memoize
	 * @return an object that memoizes the state of the member
	 */
	protected Object memoize(IMember member) {
		String src = null;
		try {
			if (!member.exists())
				return null;
			ensureOpen(member);
			src = member.getSource();
			if(src==null)
				throw new IllegalStateException("oops... no source!");
			//src = CommentScrubber.strip(src);
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return src.hashCode();
	}
	
	
	
	/* (non-Javadoc)
	 * @see edu.pdx.cs.multiview.jdt.delta.IMemberInfo#addListener(edu.pdx.cs.multiview.jdt.delta.JavaMemberModel.IModelListener)
	 */
	public void addListener(MemberModel.IModelListener listener) {
		_listeners.add(listener);
	}
	
	/* (non-Javadoc)
	 * @see edu.pdx.cs.multiview.jdt.delta.IMemberInfo#removeListener(edu.pdx.cs.multiview.jdt.delta.ListenerTest.ModelListener)
	 */
	public void removeListener(IModelListener listener) {
		_listeners.remove(listener);
	}
	
	/* (non-Javadoc)
	 * @see edu.pdx.cs.multiview.jdt.delta.IMemberInfo#getListeners()
	 */
	public Set<IModelListener> getListeners() {
		return _listeners;
	}
	
	/* (non-Javadoc)
	 * @see edu.pdx.cs.multiview.jdt.delta.IMemberInfo#notifyListeners(edu.pdx.cs.multiview.jdt.delta.MemberDelta)
	 */
	public void notifyListeners(IMemberDelta delta) {
		for (MemberModel.IModelListener listener : _listeners) {
			listener.notify(delta);
		}
	}
	
	
	
	private void ensureOpen(IMember member) throws JavaModelException {
		ICompilationUnit icu = member.getCompilationUnit();
		if (!icu.isOpen())
			icu.open(null);
	}

	public static IMemberDelta update(String handle) {
		IMember member = (IMember)JavaCore.create(handle);
		return update(member);
	}

	/**
	 * Update the cache of this member.
	 * @param member
	 * @return the delta
	 */
	public static IMemberDelta update(IMember member) {
		String handleId = member.getHandleIdentifier();
		MemberInfo fresh = createInfo(handleId);
		MemberInfo old  = CACHE.get(handleId);
		//if the member is not cached its an addition, otherwise compare
		IMemberDelta delta  = (old == null) ? createAddedDelta(fresh) : old.compare(fresh);
		CACHE.put(handleId, fresh);
		return delta;
	}

	//--------------------------------------------------------------------------------
	//Utility factory methods
	//--------------------------------------------------------------------------------
	//FIXME:these should all go away now...
	
	/**
	 * @return a MemberInfo object that describes the member identfied by this handle
	 */
	public static MemberInfo createInfo(String handle) {
		IJavaElement je = JavaCore.create(handle);
		return createInfo((IMember)je);
	}
	
	public static MemberInfo createInfo(IMember member) {
		//sigh... an ugly switch:
		if (member instanceof IField) {
			return new FieldInfo((IField)member);
		}
		if (member instanceof IMethod) {
			return new MethodInfo((IMethod)member);
		}		
		if (member instanceof IType)
			return new TypeInfo((IType)member);
		return null; //FIXME: shouldn't happen
	}
	
	public MemberDelta createModifiedDelta(IMemberInfo other) {
		return new MemberDelta(this, DeltaKind.MODIFIED);
	}

	public MemberDelta createUnchangedDelta(MemberInfo info) {
		return new MemberDelta(info, DeltaKind.UNCHANGED);
	}

	public MemberDelta createRemovedDelta(MemberInfo info) {
		return new MemberDelta(info, DeltaKind.REMOVED);
	}

	public static MemberDelta createAddedDelta(IMemberInfo info) {
		return new MemberDelta(info, DeltaKind.ADDED);
	}


	@Override
	public String toString(){
		return getHandle()+"->"+getListeners();
	}
	
}
