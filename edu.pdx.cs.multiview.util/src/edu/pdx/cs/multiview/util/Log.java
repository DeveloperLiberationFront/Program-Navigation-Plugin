package edu.pdx.cs.multiview.util;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class Log {

	private String pluginId; 
	
	protected Log(){}
	
	public void logInfo(String message) {
		log(IStatus.INFO, IStatus.OK, message, null);

	}

	public void logError(Throwable exception) {
		logError("Unexpected Exception", exception);

	}

	public void logError(String message, Throwable exception) {
		exception.printStackTrace();
		log(IStatus.ERROR, IStatus.OK, message, exception);
	}

	public void log(int severity, int code, String message,Throwable exception) {
		log(createStatus(severity, code, message, exception));
	}

	public IStatus createStatus(int severity, int code, String message,
			Throwable exception) {
		return new Status(severity, pluginId, code, message,exception);
	}

	public void log(IStatus status) {
		if(status.getException()!=null)
			status.getException().printStackTrace();
		else if(status.getMessage()!=null)
			System.err.println(status.getMessage());
		ResourcesPlugin.getPlugin().getLog().log(status);
	}
	
	public static Log getLog(String pluginId){
		Log l = new Log();
		l.pluginId = pluginId;
		return l;
	}
}