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
package org.eclipse.epp.usagedata.internal.recording.settings;

import org.eclipse.epp.usagedata.internal.recording.filtering.NullFilter;
import org.eclipse.epp.usagedata.internal.recording.filtering.UsageDataEventFilter;

public interface UploadSettings {

	/**
	 * This method answers whether or not we want to ask the server to 
	 * provide a log of activity. 
	 * 
	 * @return true if we're logging, false otherwise.
	 */
	public abstract boolean isLoggingServerActivity();

	/**
	 * This method returns the target URL for uploads.
	 * 
	 * @return the target URL for uploads.
	 */
	public abstract String getUploadUrl();

	/**
	 * This method returns the receiver's filter. A filter
	 * is <strong>always</strong> returned. If no filter is required,
	 * consider returning an instance of {@link NullFilter}.
	 * 
	 * @return an instance of a class that implements {@link UsageDataEventFilter}
	 */
	public abstract UsageDataEventFilter getFilter();

	public abstract boolean hasUserAcceptedTermsOfUse();

	public abstract boolean isEnabled();

	public abstract String getUserId();

	public abstract String getWorkspaceId();

	public abstract String getUserAgent();

}