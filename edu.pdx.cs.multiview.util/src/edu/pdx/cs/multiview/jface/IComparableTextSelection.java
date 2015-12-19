package edu.pdx.cs.multiview.jface;

import org.eclipse.jface.text.ITextSelection;

public interface IComparableTextSelection extends ITextSelection {

	public static enum COMPARISON{
		NO_OVERLAP,
		SOME_OVERLAP,
		FULL_CONTAINMENT,
		FULL_CONTAINER
	};
	
	/**
	 * Compares myself to the argument
	 * 
	 * @param s
	 * 
	 * @return		{@link COMPARISON}.NO_OVERLAP	if I don't overlap with s
	 * 				{@link COMPARISON}.SOME_OVERLAP	if I overlap with some of s
	 * 				{@link COMPARISON}.NO_OVERLAP	if I am fully within x
	 */
	public COMPARISON compareTo(ITextSelection s);
}
