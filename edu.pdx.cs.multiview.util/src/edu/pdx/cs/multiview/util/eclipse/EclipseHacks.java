package edu.pdx.cs.multiview.util.eclipse;

import java.lang.reflect.Field;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;


@SuppressWarnings("restriction")
public class EclipseHacks {

	/**
	 * Uses reflection to get the AnnotationPainter from a decorated text editor
	 * 
	 * @param editor
	 * @return 
	 */
	public static AnnotationPainter getAnnotationPainter(AbstractDecoratedTextEditor editor) {
		
		try {
			Field fSourceViewerDecorationSupport = AbstractDecoratedTextEditor.class.
								getDeclaredField("fSourceViewerDecorationSupport");
			
			fSourceViewerDecorationSupport.setAccessible(true);
			SourceViewerDecorationSupport s = (SourceViewerDecorationSupport)
										fSourceViewerDecorationSupport.get(editor);
			Field fAnnotationPainter = s.getClass().
											getDeclaredField("fAnnotationPainter");
			fAnnotationPainter.setAccessible(true);
			
			return  (AnnotationPainter)fAnnotationPainter.get(s);
			
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static SourceViewer getSourceViewer(AbstractTextEditor ed) {
		
		if(ed instanceof JavaEditor)
			return (SourceViewer)((JavaEditor)ed).getViewer();
		
		try {
			Field fSourceViewer = AbstractTextEditor.class.
								getDeclaredField("fSourceViewer");
			
			fSourceViewer.setAccessible(true);
			SourceViewer s = (SourceViewer)
										fSourceViewer.get(ed);
			
			return  s;
			
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public static CompilationUnit getRoot(ExtractMethodRefactoring refactoring) {
		
		try {
			Field fRoot = ExtractMethodRefactoring.class.getDeclaredField("fRoot");
			fRoot.setAccessible(true);
			
			return (CompilationUnit)
				fRoot.get(refactoring);
			
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		
		return null;
	}

}
