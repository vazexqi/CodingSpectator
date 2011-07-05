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
package org.eclipse.epp.usagedata.internal.recording.uploading;

import java.io.File;

import org.eclipse.epp.usagedata.internal.recording.filtering.UsageDataEventFilter;
import org.eclipse.epp.usagedata.internal.recording.settings.UploadSettings;

public class UploadParameters {

	private File[] files;
	private UploadSettings settings;
	
	public void setSettings(UploadSettings settings) {
		this.settings = settings;
	}

	public void setFiles(File[] files) {
		this.files = files;
	}

	public UploadSettings getSettings() {
		return settings;
	}

	public File[] getFiles() {
		return files;
	}

	public UsageDataEventFilter getFilter() {
		return settings.getFilter();
	}
}
