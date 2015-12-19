package edu.pdx.cs.multiview.util.editor;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import edu.pdx.cs.multiview.jdt.util.JDTUtils;
import edu.pdx.cs.multiview.jface.ICompilationUnitListener;

public class CUEditorSelectionManager extends EditorSelectionManager{
	
	private ICompilationUnitListener listener;

	public CUEditorSelectionManager(){}
	
	public CUEditorSelectionManager(ICompilationUnitListener listener) {
		this.addSelectionChangedListener(listener);
		this.listener = listener;
	}
	
	@Override
	public void partActivated(IWorkbenchPart part) {
		boolean differentPart = part!=getEditor();
		super.partActivated(part);
		if(part instanceof AbstractTextEditor && differentPart){
			signalCUChange();
		}else{
			if(noOpenEditor(part.getSite().getWorkbenchWindow()))
				signalNoCU();
		}
	}


	private void signalCUChange() {
		if(listener!=null)
			listener.compilationUnitChanged(JDTUtils.getCUSource(getEditor()));
	}
	
	private void signalNoCU(){
		if(listener!=null)
			listener.compilationUnitChanged(null);
	}

	@Override
	public AbstractDecoratedTextEditor getEditor(){
		return (AbstractDecoratedTextEditor)super.getEditor();
	}

	public ITextSelection getSelection() {
		return (ITextSelection)getEditor().getSelectionProvider().getSelection();
	}
}
