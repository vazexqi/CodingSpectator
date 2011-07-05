/*******************************************************************************
 * Copyright (c) 2008 The Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    The Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.epp.usagedata.internal.ui.preview;

import org.eclipse.epp.usagedata.internal.gathering.events.UsageDataEvent;
import org.eclipse.epp.usagedata.internal.recording.uploading.UploadParameters;

class UsageDataEventWrapper {

	private final UsageDataEvent event;
	Boolean isIncludedByFilter = null;
	private final UploadParameters parameters;

	public UsageDataEventWrapper(UploadParameters parameters, UsageDataEvent event) {
		this.parameters = parameters;
		this.event = event;
	}

	public String getKind() {
		return event.kind;
	}

	public String getBundleId() {
		return event.bundleId;
	}

	public String getBundleVersion() {
		return event.bundleVersion;
	}

	public long getWhen() {
		return event.when;
	}

	public String getDescription() {
		return event.description;
	}

	public String getWhat() {
		return event.what;
	}

	public synchronized boolean isIncludedByFilter() {
		if (isIncludedByFilter == null) {
			isIncludedByFilter = parameters.getFilter().includes(event);
		}
		return isIncludedByFilter;
	}

	public synchronized void resetCaches() {
		isIncludedByFilter = null;
	}
}