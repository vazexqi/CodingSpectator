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
package org.eclipse.epp.usagedata.internal.gathering.monitors;

import org.eclipse.epp.usagedata.internal.gathering.UsageDataCaptureActivator;
import org.eclipse.epp.usagedata.internal.gathering.services.UsageDataService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

/**
 * Instances of this class hook into the {@link BundleContext} so
 * that they are notified of bundle events. Those events are passed
 * to the {@link UsageDataService}.
 * 
 * @author Wayne Beaton
 *
 */
public class BundleUsageMonitor implements UsageMonitor {

	private static final String BUNDLE_VERSION = "Bundle-Version"; //$NON-NLS-1$
	private static final String UNKNOWN = "unknown"; //$NON-NLS-1$
	private static final String UPDATED = "updated"; //$NON-NLS-1$
	private static final String UNRESOLVED = "unresolved"; //$NON-NLS-1$
	private static final String UNINSTALLED = "uninstalled"; //$NON-NLS-1$
	private static final String STOPPING = "stopping"; //$NON-NLS-1$
	private static final String STOPPED = "stopped"; //$NON-NLS-1$
	private static final String STARTING = "starting"; //$NON-NLS-1$
	private static final String RESOLVED = "resolved"; //$NON-NLS-1$
	private static final String LAZY_ACTIVATION = "lazy_activation"; //$NON-NLS-1$
	private static final String INSTALLED = "installed"; //$NON-NLS-1$
	private static final String STARTED = "started"; //$NON-NLS-1$
	private static final String BUNDLE = "bundle"; //$NON-NLS-1$
	private BundleListener bundleUsageListener;

	public void startMonitoring(final UsageDataService usageDataService) {
		// First, create events for all the bundles that have already been registered.
		recordCurrentlyActiveBundles(usageDataService);
		
		// Create an install a listener on the bundle context.
		bundleUsageListener = new BundleListener() {
			public void bundleChanged(BundleEvent event) {
				usageDataService.recordEvent(getWhatHappenedString(event), BUNDLE, event.getBundle().getSymbolicName(), event.getBundle().getSymbolicName(), getBundleVersion(event));
			}			
		};
		getBundleContext().addBundleListener(bundleUsageListener);
	}


	private void recordCurrentlyActiveBundles(UsageDataService usageDataService) {
		for (Bundle bundle : getBundleContext().getBundles()) {
			if (bundle.getState() != Bundle.ACTIVE) continue;
			String bundleId = bundle.getSymbolicName();
			usageDataService.recordEvent(STARTED, BUNDLE, bundleId, bundleId, getBundleVersion(bundle));
		}
	}

	/**
	 * This method returns a {@link String} that describes what caused this
	 * {@link BundleEvent} to be fired.
	 * 
	 * @param event
	 *            instance of {@link BundleEvent}.
	 * @return {@link String} describing what happened.
	 */
	protected String getWhatHappenedString(BundleEvent event) {
		switch (event.getType()) {
			case BundleEvent.INSTALLED: return INSTALLED;
			case BundleEvent.LAZY_ACTIVATION: return LAZY_ACTIVATION;
			case BundleEvent.RESOLVED: return RESOLVED;
			case BundleEvent.STARTED: return STARTED;
			case BundleEvent.STARTING: return STARTING;
			case BundleEvent.STOPPED: return STOPPED;
			case BundleEvent.STOPPING: return STOPPING;
			case BundleEvent.UNINSTALLED: return UNINSTALLED;
			case BundleEvent.UNRESOLVED: return UNRESOLVED;
			case BundleEvent.UPDATED: return UPDATED;
			default: return UNKNOWN;
		}
	}

	protected String getBundleVersion(BundleEvent event) {
		return getBundleVersion(event.getBundle());
	}

	private String getBundleVersion(Bundle bundle) {
		return (String)bundle.getHeaders().get(BUNDLE_VERSION);
	}


	public void stopMonitoring() {
		getBundleContext().removeBundleListener(bundleUsageListener);
	}	

	private BundleContext getBundleContext() {
		return UsageDataCaptureActivator.getDefault().getBundle().getBundleContext();
	}

}
