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
package org.eclipse.epp.usagedata.internal.recording;

import org.eclipse.epp.usagedata.internal.recording.uploading.UploadManager;
import org.eclipse.ui.IStartup;

/**
 * 
 * @author Mohsen Vakilian, nchen - Added transfer of UDC data to CodingSpectator watched directory
 *         on startup
 * 
 */
public class Startup implements IStartup {
	public void earlyStartup() {
		//CODINGSPECTATOR
		UploadManager manager= UsageDataRecordingActivator.getDefault().getUploadManager();
		manager.startTransferToCodingSpectator();
	}
}
