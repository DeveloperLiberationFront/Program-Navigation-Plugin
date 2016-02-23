package edu.pdx.cs.multiview.jface;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;

public class ComparisonTextSelection extends TextSelection 
									implements IComparableTextSelection{


	public static COMPARISON name1(
			ComparisonTextSelection comparisonTextSelection,
			ITextSelection selection) {
		return comparisonTextSelection.compareTo(selection);
	}

	public ComparisonTextSelection(int offset, int length) {
		super(offset, length);
	}
	
	public ComparisonTextSelection(ITextSelection other){
		super(other.getOffset(),other.getLength());
	}

	public COMPARISON compareTo(ITextSelection selection) {
		

		int myStart = this.getOffset();
		int myEnd = myStart+this.getLength();
		int otherStart = selection.getOffset();
		int otherEnd =  otherStart+selection.getLength();
		
		//I am contained
		if(myStart>=otherStart && myEnd<=otherEnd)
			return COMPARISON.FULL_CONTAINMENT;
		
		//my start is contained
		if(myStart>=otherStart && myStart<=otherEnd)
			
			return COMPARISON.SOME_OVERLAP;
		
		//my end is contained
		if(myEnd>=otherStart && myEnd <= otherEnd)
			
			return COMPARISON.SOME_OVERLAP;
		

		//other is contained
		if(myStart<=otherStart && myEnd>=otherEnd)
			return COMPARISON.FULL_CONTAINER;
		
		return COMPARISON.NO_OVERLAP;
	}

}