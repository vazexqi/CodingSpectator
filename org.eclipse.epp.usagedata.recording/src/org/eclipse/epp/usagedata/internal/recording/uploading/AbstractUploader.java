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

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.epp.usagedata.internal.recording.uploading.codingspectator.TransferToCodingSpectatorListener;

/**
 * 
 * @author Mohsen Vakilian, nchen - Added listeners for transferring UDC data to CodingSpectator.
 * 
 */
public abstract class AbstractUploader implements Uploader {

	private ListenerList uploadListeners= new ListenerList();

	private UploadParameters uploadParameters;

	public AbstractUploader() {
	}

	public void addUploadListener(UploadListener listener) {
		uploadListeners.add(listener);
	}

	public void removeUploadListener(UploadListener listener) {
		uploadListeners.remove(listener);
	}

	protected void fireUploadComplete(UploadResult result) {
		for (Object listener : uploadListeners.getListeners()) {
			((UploadListener)listener).uploadComplete(result);
		}
	}

	public UploadParameters getUploadParameters() {
		return uploadParameters;
	}

	public void setUploadParameters(UploadParameters uploadParameters) {
		this.uploadParameters= uploadParameters;
	}

	protected void checkValues() {
		if (uploadParameters == null)
			throw new RuntimeException("The UploadParameters must be set."); //$NON-NLS-1$
	}

	/////////////////
	//CODINGSPECTATOR
	/////////////////

	private ListenerList transferToCodingSpectatorListeners= new ListenerList();

	public void addTransferToCodingSpectatorListener(TransferToCodingSpectatorListener listener) {
		transferToCodingSpectatorListeners.add(listener);
	}

	public void removeTransferToCodingSpectatorListener(TransferToCodingSpectatorListener listener) {
		transferToCodingSpectatorListeners.remove(listener);
	}

	protected void fireTransferToCodingSpectatorComplete() {
		for (Object listener : transferToCodingSpectatorListeners.getListeners()) {
			((TransferToCodingSpectatorListener)listener).transferToCodingSpectatorComplete();
		}
	}
}
