package edu.pdx.cs.multiview.jdt.delta;

import java.util.Set;

import edu.pdx.cs.multiview.jdt.delta.MemberModel.IModelListener;


public interface IMemberInfo {

	/** @return the memo describing the state of the member (before change)  
	 */
	Object getMemo();

	/** @return the handle identifier of the underlying member 
	 */
	String getHandle();
	
	
	/**
	 * Compare this member info with another adn describe differences in a delta object
	 * @param other - the other member info
	 * @return the delta
	 */
	IMemberDelta compare(IMemberInfo other);

	void addListener(IModelListener listener);

	void notifyListeners(IMemberDelta delta);

	Set<IModelListener> getListeners();

	void removeListener(IModelListener listener);

}
