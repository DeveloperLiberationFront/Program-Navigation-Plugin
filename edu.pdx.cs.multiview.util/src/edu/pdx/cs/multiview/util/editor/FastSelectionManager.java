package edu.pdx.cs.multiview.util.editor;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

/**
 * I tell my listeners when the selection has changed, as soon
 * as the cursor moves.  Also, I throw events when the same 
 * spot is clicked twice.
 * 
 * @author emerson
 *
 */
public class FastSelectionManager extends CUEditorSelectionManager{
	
	private Listener listenerAdapter = new Listener(){
		
		public void handleEvent(Event e) {
			if(getEditor()!=null){
				
				AbstractDecoratedTextEditor part = getEditor();
				ISelection selection = part.getSelectionProvider().getSelection();
				
				FastSelectionManager.this.selectionChanged(part, selection);
			}
		}
	};
	
	private Control text;
	
	@Override
	protected void addListenerToEditor(){
		
		removeListenerFromEditor();
		
		if (getEditor() != null) {
			text = getStyledText();
			if (text !=null) {
				text.addListener(SWT.MouseUp,listenerAdapter);
				text.addListener(SWT.KeyUp,listenerAdapter);
			}
		}
	}

	@Override
	protected void removeListenerFromEditor(){
		
		if(text!=null){
			text.removeListener(SWT.MouseUp, listenerAdapter);
			text.removeListener(SWT.KeyUp, listenerAdapter);
			text = null;
		}
	}
}
