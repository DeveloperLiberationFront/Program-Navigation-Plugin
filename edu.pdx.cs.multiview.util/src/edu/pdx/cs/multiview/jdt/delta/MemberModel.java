package edu.pdx.cs.multiview.jdt.delta;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;

import edu.pdx.cs.multiview.jdt.delta.IMemberDelta.DeltaKind;
import edu.pdx.cs.multiview.util.Debug;

public class MemberModel implements IElementChangedListener { 
	
	/**
	 * An interface to be implemented by model listeners
	 */
	public interface IModelListener {
		void notify(IMemberDelta delta);
	}

	/** The singelton instance */
	private static final MemberModel _instance = new MemberModel();
	
	/** The cache of members */
	private MemberStore _store = new MemberStore();
	
	/**
	 * Create an instance.
	 */
	public MemberModel() {
		connect();
	}
	
	/**
	 * Connect this model to the Java Model.
	 */
	private void connect() {
		JavaCore.addElementChangedListener(this,ElementChangedEvent.POST_CHANGE);
	}
	
	/**
	 * Register a model change listener to listen to changes to this member.
	 * @param member 
	 * @param kind 
	 * @param listener
	 */
	public static void addListener(IMember member, IModelListener listener) {
		getInstance().getStore().addListener(member,listener);
	}
		
	/**
	 * Remove the given listener from this member.
	 * @param member
	 * @param listener
	 */
	public static void removeListener(IMember member, IModelListener listener) {
		getInstance().getStore().removeListener(member,listener);
	}
	
	
	/** 
	 * @return the backing member store 
	 */
	private MemberStore getStore() {
		return _store;
	}

	/**
	 * @return the singelton instance
	 */
	private static MemberModel getInstance() {
		return _instance;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IElementChangedListener#elementChanged(org.eclipse.jdt.core.ElementChangedEvent)
	 */
	public void elementChanged(ElementChangedEvent event) {

		checkForTypeChange(event.getDelta());
		
		IJavaElementDelta jdelta = event.getDelta();
		IType type = getAffectedType(jdelta);
		if (type == null) {
			Debug.debug("no type associated with element change event");
			return;
		}
		
		//get children of the changed element
		IMemberSet children = _store.getChildren(type);
		if (children == null) //if there are none, return
			return;
		//for each child, update the store and in the event of a change
		//notify listeners
		for (IMemberInfo child : children.getMembers()) {
			try{
				//update the store
				IMemberDelta delta = update(child.getHandle());
				//if the udpdate is a change, notify listeners
				if (!delta.getKind().equals(DeltaKind.UNCHANGED))
					child.notifyListeners(delta);
			}catch (Exception e){
				//TODO: an error will be thrown if something in the
				//		child cannot be memoized.  This is not an error
				//		case, if a child has been deleted
				Debug.debug(e.getMessage());
			}
		}
		
	}

	/**
	 * Dispatch to any listeners if any types were added or removed
	 * 
	 * @param delta
	 */
	private void checkForTypeChange(IJavaElementDelta delta) {
		
		if(delta.getKind()==IJavaElementDelta.ADDED){
			if(delta.getElement() instanceof IType)
				fireTypeChange((IType)delta.getElement(),DeltaKind.ADDED);
			else if(delta.getElement() instanceof ICompilationUnit)
				fireTypeChange(((ICompilationUnit)delta.getElement()).findPrimaryType(),DeltaKind.ADDED);
		}else if(delta.getKind()==IJavaElementDelta.REMOVED){
			if(delta.getElement() instanceof IType)
				fireTypeChange((IType)delta.getElement(),DeltaKind.REMOVED);
			else if(delta.getElement() instanceof ICompilationUnit)
				fireTypeChange(((ICompilationUnit)delta.getElement()).findPrimaryType(),DeltaKind.REMOVED);
		}
			
		
		for(IJavaElementDelta cDelta : delta.getAffectedChildren())
			checkForTypeChange(cDelta);
		
	}

	/**
	 * Get the type associated with this delta.
	 * @param jdelta - the delta
	 * @return - the associated type
	 */
	private IType getAffectedType(IJavaElementDelta jdelta) {
		if (jdelta.getElement() instanceof IType)
			return (IType)jdelta.getElement();
		else if(jdelta.getElement() instanceof ICompilationUnit){
			return ((ICompilationUnit)jdelta.getElement()).findPrimaryType();
		}
		IType type = null;
		for (IJavaElementDelta delta : jdelta.getAffectedChildren()) {
			type = getAffectedType(delta);
			if (type != null)
				return type;
		}
		
		return null;
	}


	/**
	 * Update the cache of this member.
	 * @param member
	 * @return the delta
	 */
	public IMemberDelta update(IMember member) {
		String handleId = member.getHandleIdentifier();
		IMemberInfo fresh = createInfo(handleId);
		IMemberInfo old  = _store.get(handleId);
		//if the member is not cached it's an addition, otherwise compare
		IMemberDelta delta  = (old == null) ? createAddedDelta(fresh) : old.compare(fresh);
		if(!(delta.getKind()==DeltaKind.UNCHANGED)){
			copyListeners(old,fresh);
			_store.put(handleId, fresh);
		}
		return delta;
	}
	
	private void copyListeners(IMemberInfo source, IMemberInfo destination){
		for(IModelListener listener : source.getListeners())
			destination.addListener(listener);		
	}

	
	//a convenience accessor
	public IMemberDelta update(String handle) {
		IMember member = (IMember)JavaCore.create(handle);
		return update(member);
	}
	
	
	//--------------------------------------------------------------------------------
	//Utility factory methods
	//--------------------------------------------------------------------------------
	
	/**
	 * @return a MemberInfo object that describes the member identfied by this handle
	 */
	public static IMemberInfo createInfo(String handle) {
		IJavaElement je = JavaCore.create(handle);
		return createInfo((IMember)je);
	}
	
	public static IMemberInfo createInfo(IMember member) {
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
	
	public static IMemberDelta createModifiedDelta(MemberInfo info) {
		return new MemberDelta(info, DeltaKind.MODIFIED);
	}

	public static IMemberDelta createUnchangedDelta(MemberInfo info) {
		return new MemberDelta(info, DeltaKind.UNCHANGED);
	}

	public static IMemberDelta createRemovedDelta(MemberInfo info) {
		return new MemberDelta(info, DeltaKind.REMOVED);
	}

	public static IMemberDelta createAddedDelta(IMemberInfo info) {
		return new MemberDelta(info, DeltaKind.ADDED);
	}

	/**
	 * @param javaElement
	 * 
	 * @return	whether the parameter can be listened to
	 */
	public static boolean canListenTo(IJavaElement javaElement) {
		
		return javaElement instanceof IField || javaElement instanceof IMethod;
	}

	private static List<IModelListener> typeListeners = new ArrayList<IModelListener>();
	
	public static void addListenerForTypeChange(IModelListener listener) {
		typeListeners.add(listener);
	}
	
	private void fireTypeChange(IType type, DeltaKind kind) {
		for(IModelListener listener : typeListeners)
			listener.notify(new MemberDelta(MemberInfo.createInfo(type),kind));
	}



}
