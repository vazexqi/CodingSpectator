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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.epp.usagedata.internal.recording.UsageDataRecordingActivator;
import org.eclipse.epp.usagedata.internal.recording.settings.UsageDataRecordingSettings;
import org.eclipse.ui.PlatformUI;

public class UploadManager {

	public static final int UPLOAD_STARTED_OK = 0;
	public static final int NO_FILES_TO_UPLOAD = 1;
	public static final int UPLOAD_IN_PROGRESS = 2;
	public static final int WORKBENCH_IS_CLOSING = 3;
	public static final int NO_UPLOADER = 4;
	public static final int UPLOAD_DISABLED = 5;
	
	private Object lock = new Object();
	private Uploader uploader;
	private ListenerList uploadListeners = new ListenerList();

	/**
	 * This method starts the upload. The first thing it does is find the files
	 * containing data that needs to be uploaded. If no data is found, then the
	 * method simply returns and the universe is left to unfold as it will. If
	 * data is found, we continue.
	 * <p>
	 * The settings are checked to see what the user wants us to do with the
	 * data. If the user has authorized that the data be uploaded, an upload job
	 * is spawned. If the settings indicate that the user must be asked what to
	 * do, then an editor is opened which invites the user to decide what to do
	 * with the information.
	 * </p>
	 * <p>
	 * This method returns a status code. The value is
	 * {@link #UPLOAD_IN_PROGRESS} if an upload is already in progress when the
	 * request is made, {@link #NO_FILES_TO_UPLOAD} if no files are available
	 * for upload, {@link #WORKBENCH_IS_CLOSING} if the workbench is closing at
	 * the time the request is made, {@link #NO_UPLOADER} if an uploader cannot
	 * be found, or {@value #UPLOAD_STARTED_OK} if a new upload is started.
	 * </p>
	 * 
	 * @return a status code.
	 */
	public int startUpload() {
		if (!getSettings().isEnabled()) return UPLOAD_DISABLED;
		if (PlatformUI.getWorkbench().isClosing()) return WORKBENCH_IS_CLOSING;
		
		File[] usageDataUploadFiles;
		synchronized (lock) {
			if (uploader != null) return UPLOAD_IN_PROGRESS;
			
			usageDataUploadFiles = findUsageDataUploadFiles();
			if (usageDataUploadFiles.length == 0) return NO_FILES_TO_UPLOAD;
			
			uploader = getUploader();
			if (uploader == null) return NO_UPLOADER;
		}
		
		getSettings().setLastUploadTime();
		
		UploadParameters uploadParameters = new UploadParameters();
		uploadParameters.setSettings(getSettings());
		uploadParameters.setFiles(usageDataUploadFiles);
		//request.setFilter(getSettings().getFilter());
		
		uploader.setUploadParameters(uploadParameters);
		
		/*
		 * Add a listener to the new uploader so that it will notify
		 * us when it is complete. Then, we'll notify our own listeners.
		 */
		uploader.addUploadListener(new UploadListener() {
			public void uploadComplete(UploadResult result) {
				uploader = null;
				fireUploadComplete(result);
			}
		});
		
		uploader.startUpload();
		
		return UPLOAD_STARTED_OK;
	}

	private File[] findUsageDataUploadFiles() {
		return getSettings().getUsageDataUploadFiles();
	}
	
	private UsageDataRecordingSettings getSettings() {
		return UsageDataRecordingActivator.getDefault().getSettings();
	}
	
	/**
	 * This method returns the {@link Uploader} to use to upload data to the
	 * server. At present, this is implemented using the extension point
	 * registry. This separation is done so that a UI component can participate
	 * in the upload process without muddying the lines between model and view;
	 * a UI plug-in, might, for example, open an editor or dialogue box asking
	 * the user if it is okay to do the upload.
	 * <p>
	 * An extension point is not quite the right fit here as there is currently
	 * only provision for there being a single choice. It's a good choice, 
	 * because we want this to be lazy loaded. At some point, it may
	 * make sense to have multiple uploaders available and let the user
	 * (via preferences) select the one that they want to use (perhaps using
	 * a combo box or something. For now, that's probably just too complicated,
	 * and so the extension point is used with an understanding that there
	 * should only be one extension to it (in the event that there is more than
	 * one extension, the first one that we find is used).
	 * </p>
	 * 
	 * @return
	 */
	private Uploader getUploader() {
		IConfigurationElement[] elements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(UsageDataRecordingActivator.PLUGIN_ID + ".uploader"); //$NON-NLS-1$
		for (IConfigurationElement element : elements) {
			if ("uploader".equals(element.getName())) { //$NON-NLS-1$
				try {
					Object uploader = element.createExecutableExtension("class"); //$NON-NLS-1$
					if (uploader instanceof Uploader) {
						return (Uploader) uploader;
					}
				} catch (CoreException e) {
					UsageDataRecordingActivator.getDefault().getLog().log(e.getStatus());
				}
			}
		}
		return null;
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
}
