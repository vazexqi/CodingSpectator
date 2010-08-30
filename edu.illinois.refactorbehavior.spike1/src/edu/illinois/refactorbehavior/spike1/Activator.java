package edu.illinois.refactorbehavior.spike1;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.IRefactoringExecutionListener;
import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryService;
import org.eclipse.ltk.core.refactoring.history.RefactoringExecutionEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "edu.illinois.refactorbehavior.spike2"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		IRefactoringHistoryService historyService = RefactoringCore.getHistoryService();
		historyService.addExecutionListener(new IRefactoringExecutionListener() {

			@Override
			public void executionNotification(RefactoringExecutionEvent event) {
				IProgressMonitor progressMonitor = new NullProgressMonitor();
				RefactoringDescriptorProxy descriptor = event.getDescriptor();
				RefactoringDescriptor requestDescriptor = descriptor.requestDescriptor(progressMonitor);

				if (event.getEventType() == RefactoringExecutionEvent.PERFORMED) {
					System.err.println("Refactoring performed");
				}

				if (event.getEventType() == RefactoringExecutionEvent.REDONE) {
					System.err.println("Refactoring redone");
				}

				if (event.getEventType() == RefactoringExecutionEvent.UNDONE) {
					System.err.println("Refactoring undone");
				}

				System.err.println(requestDescriptor.toString());

			}
		});
		
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
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
