/*******************************************************************************
 * Copyright (c) 2009 The Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    The Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.epp.usagedata.internal.gathering.monitors;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.epp.usagedata.internal.gathering.services.UsageDataService;

/**
 * The {@link LogMonitor} class records messages that are
 * written to the log. Only messages with a severity of {@link IStatus#ERROR}
 * are recorded.
 *
 * @see IStatus#ERROR
 * @see Platform#addLogListener(ILogListener)
 */
public class LogMonitor implements UsageMonitor {
	
	private static final String WHAT_ERROR = "error"; //$NON-NLS-1$
	private static final String KIND_LOG = "log"; //$NON-NLS-1$
	
	private UsageDataService usageDataService;
	ILogListener listener = new ILogListener() {
		public void logging(IStatus status, String plugin) {
			if (status.getSeverity() != IStatus.ERROR) return;
			usageDataService.recordEvent(WHAT_ERROR, KIND_LOG, status.getMessage(), null);
		}
	};
	
	public void startMonitoring(UsageDataService usageDataService) {
		this.usageDataService = usageDataService;
		Platform.addLogListener(listener);
	}

	public void stopMonitoring() {
		Platform.removeLogListener(listener);
	}

}
