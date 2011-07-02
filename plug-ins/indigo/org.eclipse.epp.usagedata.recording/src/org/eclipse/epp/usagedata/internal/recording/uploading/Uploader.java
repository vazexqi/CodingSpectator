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

import org.eclipse.epp.usagedata.internal.recording.uploading.codingspectator.TransferToCodingSpectatorListener;

/**
 * 
 * @author Mohsen Vakilian, nchen - Added the support to transfer UDC data to CodingSpectator.
 * 
 */
public interface Uploader {
	boolean isUploadInProgress();

	void startUpload();

	void addUploadListener(UploadListener listener);

	void removeUploadListener(UploadListener listener);

	void setUploadParameters(UploadParameters uploadParameters);

	UploadParameters getUploadParameters();

	/////////////////
	//CODINGSPECTATOR
	////////////////

	void startTransferToCodingSpectator();

	void addTransferToCodingSpectatorListener(TransferToCodingSpectatorListener listener);

	void removeTransferToCodingSpectatorListener(TransferToCodingSpectatorListener listener);

}
