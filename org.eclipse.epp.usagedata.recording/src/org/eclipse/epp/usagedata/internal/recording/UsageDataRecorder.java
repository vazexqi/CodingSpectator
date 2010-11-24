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
package org.eclipse.epp.usagedata.internal.recording;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.epp.usagedata.internal.gathering.events.UsageDataEvent;
import org.eclipse.epp.usagedata.internal.gathering.events.UsageDataEventListener;
import org.eclipse.epp.usagedata.internal.recording.settings.UsageDataRecordingSettings;
import org.eclipse.epp.usagedata.internal.recording.uploading.BasicUploader;
import org.eclipse.epp.usagedata.internal.recording.uploading.UploadManager;

public class UsageDataRecorder implements UsageDataEventListener {
	
	/**
	 * How many events do we queue up before we attempt to write them out to
	 * disk?
	 */
	private static final int EVENT_COUNT_THRESHOLD = 25;

	/**
	 * The maximum number of exceptions that the receiver can produce before it
	 * it shuts down. The idea is to avoid writing hundreds of pointless error
	 * messages into the log.
	 */
	private static final int EXCEPTION_THRESHOLD = 5;

	/**
	 * When the file holding upload data exceeds this number
	 * of bytes, it is moved so that it can be uploaded.
	 */
	private static final long FILE_SIZE_THRESHOLD = 25000; 

	/**
	 * This list holds events as they are received. Once the number of events in
	 * this list exceeds a threshold ({@link #EVENT_COUNT_THRESHOLD}), the
	 * entire contents of the list is dumped to a file. This is intended to
	 * reduce the impact of the receiver on the performance of the system by
	 * minimising access to the file system.
	 */
	private List<UsageDataEvent> events;

	/**
	 * Is the receiver in a "running" state?
	 */
	private boolean running = false;

	/**
	 * How many exceptions has the receiver generated? Once this value passes
	 * {@link #EXCEPTION_THRESHOLD}, the receiver stops itself.
	 */
	private int exceptionCount = 0;

	public void start() {
		if (running) return;
		events = new ArrayList<UsageDataEvent>(EVENT_COUNT_THRESHOLD);
		running = true;
	}

	public synchronized void stop() {
		if (!running) return;
		running = false;
		dumpEvents();
		events = null;
	}
	
	public synchronized void accept(UsageDataEvent event) {
		if (event == null) return;
		if (!canAcceptEvents()) return;
		
		if (!running) return;
		events.add(event);
			
		if (events.size() >= EVENT_COUNT_THRESHOLD) dumpEvents();
		
		uploadDataIfNecessary();
	}
	
	protected void uploadDataIfNecessary() {
		if (getSettings() == null) return;
		if (!getSettings().isTimeToUpload()) return;
		
		UploadManager manager = getUploadManager();
		if (manager == null) return;
		manager.startUpload();
	}

	protected UsageDataRecordingSettings getSettings() {
		if (UsageDataRecordingActivator.getDefault() == null) return null; 
		return UsageDataRecordingActivator.getDefault().getSettings();
	}

	/**
	 * This method (curiously enough) prepares the data that's been collected by
	 * the receiver for upload. Preparing the data involves first making sure
	 * that all the events that we've recorded up to this point are properly
	 * recorded. Then, the file that we've been writing events to is renamed so
	 * that it can be found by the {@link BasicUploader}. When the next
	 * event comes in, a new file will be created.
	 */
	private synchronized void prepareForUpload() {
		if (getSettings() == null) return;
		File file = getSettings().getEventFile();
		
		// If the file does not exist, then something bad has happened. Just return.
		if (!file.exists()) return;
		
		if (file.length() < FILE_SIZE_THRESHOLD) return;
		
		File destination = getSettings().computeDestinationFile();
		
		// TODO What if the rename fails?
		file.renameTo(destination);
	}

	private UploadManager getUploadManager() {
		if (UsageDataRecordingActivator.getDefault() == null) return null;
		return UsageDataRecordingActivator.getDefault().getUploadManager();
	}

	private boolean canAcceptEvents() {
		if (events == null) return false;
		return true;
	}

	protected synchronized void dumpEvents() {
		prepareForUpload();
		
		Writer writer = null;
		try {
			writer = getWriter();
			if (writer == null) return;
			for (UsageDataEvent event : events) {
				UsageDataRecorderUtils.writeEvent(writer, event);
			}
			events.clear();
		} catch (IOException e) {
			handleException(e, "Error writing events to file."); //$NON-NLS-1$
		} finally {
			close(writer);
		}
	}

	private void handleException(IOException e, String message) {
		if (exceptionCount++ > EXCEPTION_THRESHOLD) {
			UsageDataRecordingActivator.getDefault().log(IStatus.INFO, e, "The UsageDataRecorder has been stopped because it has caused too many exceptions"); //$NON-NLS-1$
			stop();
		}
		UsageDataRecordingActivator.getDefault().log(IStatus.ERROR, e, message);
	}
	

	private Writer getWriter() throws IOException {
		if (getSettings() == null) return null;
		return createEventWriter(getSettings().getEventFile());
	}

	private Writer createEventWriter(File file) throws IOException {
		if (file.exists())
			return new FileWriter(file, true);

		file.createNewFile();
		FileWriter writer = new FileWriter(file);
		UsageDataRecorderUtils.writeHeader(writer);
		
		return writer;
	}
	
	private void close(Writer writer) {
		if (writer == null) return;
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Handle exception
		}
	}
}
