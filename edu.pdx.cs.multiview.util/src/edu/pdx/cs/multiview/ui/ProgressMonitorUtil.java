package edu.pdx.cs.multiview.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.part.EditorPart;

public class ProgressMonitorUtil {
	
	public static IProgressMonitor getBackgroundMonitor(EditorPart e) {
		return getStatusLineManager(e).getProgressMonitor();	
	}

	public static IStatusLineManager getStatusLineManager(EditorPart e) {
		return e.getEditorSite().getActionBars().getStatusLineManager();
	}
	
	
}
