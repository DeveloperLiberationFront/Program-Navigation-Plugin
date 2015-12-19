package edu.pdx.cs.multiview.jface.text;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.ltk.core.refactoring.Refactoring;

public class RefactoringBundle{
	
	private Refactoring refactoring;
	private List<String> identifiers;
	private LinkedPositionGroup group;

	public RefactoringBundle(Refactoring refactoring) {
		this.refactoring = refactoring;
		identifiers = new LinkedList<String>();
	}

	public String generateIdName(String source) {		
		return generateIdNotIn(source, Caps.lower);
	}
	
	public String generateConstantName(String source) {		
		return generateIdNotIn(source, Caps.upper);
	}
	
	public String generateClassName(String source) {		
		return generateIdNotIn(source, Caps.initial);
	}

	private String generateIdNotIn(String source, Caps caps) {
		int i = 0;
		while(source.contains(caps.getId(++i))){}
		addIdentifier(caps.getId(i));
		return caps.getId(i);
	}
	
	private enum Caps{
		lower,upper,initial;
		
		public String getId(int index){			
			if(equals(upper))
				return "NAME"+index;
			else if(equals(initial))
				return "Name"+index;
			return "name"+index;
		}
	}

	public Refactoring getRefactoring() {
		return refactoring;
	}
	
	public void addIdentifier(String s){
		identifiers.add(s);
	}
	
	public LinkedPositionGroup getPositionGroup(IDocument document) throws BadLocationException{
		
		if(group==null)
			initGroup(document);
		
		return group;
	}

	private void initGroup(IDocument document) throws BadLocationException {
		
		group = new LinkedPositionGroup();
		
		for(String id : identifiers){
			String source = document.get();
			for(int i = source.indexOf(id); i > -1; i = source.indexOf(id,i+1)){
				LinkedPosition p = new LinkedPosition(document,i,id.length());
				group.addPosition(p);
			}
		}
	}

	public LinkedPosition getFirstPosition(IDocument document) throws BadLocationException{
		return getPositionGroup(document).getPositions()[0];
	}
}