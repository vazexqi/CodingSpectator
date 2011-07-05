/*******************************************************************************
 * Copyright (c) 2007 The Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    The Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.epp.usagedata.internal.ui;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.epp.usagedata.ui"; //$NON-NLS-1$

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
	
	public ImageDescriptor getImageDescriptor(String path) {
		ImageDescriptor descriptor = getImageRegistry().getDescriptor(path);
		if (descriptor == null) {
			descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(getBundle().getSymbolicName(), path);
			getImageRegistry().put(path, descriptor);
		}
		return descriptor;
	}
	
	public void log(int status, String message, Object ... arguments) {
		log(status, (Exception)null, message, arguments);
	}
	
	public void log(int status, Exception exception, String message, Object ... arguments) {
		log(status, exception, String.format(message, arguments));
	}
	
	public void log(int status, Exception e, String message) {
		getLog().log(new Status(status, PLUGIN_ID, message, e));
	}
	

	public void log(Status status) {
		getLog().log(status);
	}
}
