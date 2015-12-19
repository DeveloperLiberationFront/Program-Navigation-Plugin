package dataTool;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import edu.pdx.cs.multiview.util.Log;

/**
 * Plugin overhead - don't mess with this
 */
public class Activator extends AbstractUIPlugin {

	private static final String ID = "dataTool";
	//The shared instance.
	private static Activator plugin;
	
	public Activator() {	plugin = this;	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(ID, path);
	}
	
	private static Log log  = Log.getLog(ID);
	
	public static void logInfo(String message) {
		log.logInfo(message);
	}

	public static void logError(Throwable exception) {
		log.logError(exception);
	}

	public static void logError(String message, Throwable exception) {
		log.logError(message, exception);
	}
}
