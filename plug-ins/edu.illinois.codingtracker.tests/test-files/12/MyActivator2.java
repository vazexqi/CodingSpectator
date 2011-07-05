package p1.p2;


import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class MyActivator2 extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "edu.illinois.test1"; //$NON-NLS-1$

	// The shared instance
	private static MyActivator2 plugin;
	
	/**
	 * The constructor
	 */
	public MyActivator2() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static MyActivator2 getDefault() {
		return plugin;
	}

}
