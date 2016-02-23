package edu.pdx.cs.multiview.jface;

import org.eclipse.jface.viewers.ISelectionChangedListener;

/**
 * I listen to selection change events and underlying compilation
 * unit changes
 * 
 * @author emerson
 */
public interface ICompilationUnitListener extends ISelectionChangedListener{

	/**
	 * A compilation unit has changed
	 * 
	 * @param source	the full source code that has changed of the CU
	 */
	public void compilationUnitChanged(String source);
}
