package edu.pdx.cs.multiview.jface;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Instances of me support objects that listen for property changes
 * 
 * @author emerson
 *
 */
public interface IPropertyChangeSource {

	/**
	 * @see PropertyChangeSupport#addPropertyChangeListener(PropertyChangeListener)
	 * 
	 * @param listener
	 */
	public abstract void addPropertyChangeListener(
			PropertyChangeListener listener);

	/**
	 * @see PropertyChangeSupport#removePropertyChangeListener(PropertyChangeListener)
	 * 
	 * @param listener
	 */
	public abstract void removePropertyChangeListener(
			PropertyChangeListener listener);

}