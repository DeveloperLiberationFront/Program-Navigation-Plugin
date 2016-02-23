package edu.pdx.cs.multiview.jface.text;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.text.link.LinkedModeUI.ExitFlags;
import org.eclipse.jface.text.link.LinkedModeUI.IExitPolicy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;


public class LinkModeManager{
	
	private WrappedEditor editor;
	private List<RefactoringBundle> bundles;
	
	public LinkModeManager(WrappedEditor anEditor) {
		editor = anEditor;
		bundles = new ArrayList<RefactoringBundle>();
	}

	public void add(RefactoringBundle bundle) throws BadLocationException {
		bundles.add(bundle);
	}

	public void activateLinks(){
		
		try {
			
			LinkedModeModel model = new LinkedModeModel();
			LinkedPositionGroup firstGroup = null;
			
			for(RefactoringBundle bundle : bundles){
				LinkedPositionGroup group = bundle.getPositionGroup(editor.getDocument());
				if(!group.isEmpty()){
					if(firstGroup==null)
						firstGroup = group;
					
					model.addGroup(group);
				}
			}
			
			if(firstGroup==null)
				return;
			
			model.forceInstall();
			editor.addLinkingListener(model);
			
			ITextViewer viewer = editor.getViewer();
			LinkedModeUI ui= new EditorLinkedModeUI(model, viewer);
			ui.setExitPolicy(new DeleteBlockingExitPolicy(editor.getDocument()));
			ui.enter();
			
			LinkedPosition p = firstGroup.getPositions()[0];
			viewer.setSelectedRange(p.offset, p.length);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static class DeleteBlockingExitPolicy implements IExitPolicy {
		private IDocument fDocument;

		public DeleteBlockingExitPolicy(IDocument document) {
			fDocument= document;
		}

		public ExitFlags doExit(LinkedModeModel model, VerifyEvent event, int offset, int length) {
			if (length == 0 && (event.character == SWT.BS || event.character == SWT.DEL)) {
				LinkedPosition position= model.findPosition(new LinkedPosition(fDocument, offset, 0, LinkedPositionGroup.NO_STOP));
				if (position != null) {
					if (event.character == SWT.BS) {
						if (offset - 1 < position.getOffset()) {
							//skip backspace at beginning of linked position
							event.doit= false;
						}
					} else /* event.character == SWT.DEL */ {
						if (offset + 1 > position.getOffset() + position.getLength()) {
							//skip delete at end of linked position
							event.doit= false;
						}
					}
				}
			}
			
			return null; // don't change behavior
		}
	}
}

/**
 * An exit policy that skips Backspace and Delete at the beginning and at the end
 * of a linked position, respectively.
 * 
 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=183925 .
 * 
 * COPIED
 */
class DeleteBlockingExitPolicy implements IExitPolicy {
	private IDocument fDocument;

	public DeleteBlockingExitPolicy(IDocument document) {
		fDocument= document;
	}

	public ExitFlags doExit(LinkedModeModel model, VerifyEvent event, int offset, int length) {
		if (length == 0 && (event.character == SWT.BS || event.character == SWT.DEL)) {
			LinkedPosition position= model.findPosition(new LinkedPosition(fDocument, offset, 0, LinkedPositionGroup.NO_STOP));
			if (position != null) {
				if (event.character == SWT.BS) {
					if (offset - 1 < position.getOffset()) {
						//skip backspace at beginning of linked position
						event.doit= false;
					}
				} else /* event.character == SWT.DEL */ {
					if (offset + 1 > position.getOffset() + position.getLength()) {
						//skip delete at end of linked position
						event.doit= false;
					}
				}
			}
		}

		return null; // don't change behavior
	}
}