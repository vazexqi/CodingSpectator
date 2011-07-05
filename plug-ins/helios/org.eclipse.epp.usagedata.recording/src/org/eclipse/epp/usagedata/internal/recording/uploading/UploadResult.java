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

public class UploadResult {

	public static final int CANCELLED = 0;

	private final int returnCode;

	/**
	 * 
	 * @param returnCode
	 *            code describing result of operation; typically an HTTP return
	 *            code that results from the upload operation.
	 */
	public UploadResult(int returnCode) {
		this.returnCode = returnCode;
	}

	public int getReturnCode() {
		return returnCode;
	}

	public boolean isSuccess() {
		return returnCode == 200;
	}

}
