package edu.pdx.cs.multiview.jface.text;

import org.eclipse.jdt.internal.ui.javaeditor.EditorHighlightingSynchronizer;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.link.LinkedModeModel;


@SuppressWarnings("restriction")
public class WrappedEditor {

	protected final JavaEditor editor;

	public WrappedEditor(JavaEditor e){
		editor = e;
	}
	
	public IDocument getDocument() {
		return editor.getDocumentProvider().getDocument(editor.getEditorInput());
	}

	public void addLinkingListener(LinkedModeModel model) {
		model.addLinkingListener(new EditorHighlightingSynchronizer(editor));
	}

	public ITextViewer getViewer() {
		return editor.getViewer();
	}

}