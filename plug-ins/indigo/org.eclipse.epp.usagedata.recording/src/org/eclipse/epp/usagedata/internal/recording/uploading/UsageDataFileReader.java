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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.epp.usagedata.internal.gathering.events.UsageDataEvent;
import org.eclipse.epp.usagedata.internal.recording.UsageDataRecorderUtils;

public class UsageDataFileReader {
	public interface Iterator {
		public void header(String header) throws Exception;
		public void event(String line, UsageDataEvent event) throws Exception;
	}

	private final BufferedReader reader;
 
	/**
	 * This constructor creates an instance that will read the data contained in
	 * the <code>file</code> parameter. Note that if you use this constructor,
	 * you must explicitly {@link #close()} the resulting instance.
	 * 
	 * @param file
	 *            a {@link File}; the file must exist.
	 * @throws FileNotFoundException
	 *             if the file does not exist, or is a directory, or is some
	 *             other way a foolish choice.
	 * @throws IOException
	 */
	public UsageDataFileReader(File file) throws IOException {
		this(new FileInputStream(file));
	}

	public UsageDataFileReader(InputStream inputStream) throws IOException {
		this(new InputStreamReader(inputStream));
	}

	public UsageDataFileReader(Reader reader) throws IOException {
		this(new BufferedReader(reader));
	}
	
	public UsageDataFileReader(BufferedReader bufferedReader) throws IOException {
		reader = bufferedReader;
	}

	/**
	 * Curiously enough, this method creates and returns a
	 * {@link UsageDataEvent} object from the given {@link String}. If an error
	 * occurs while processing the string, <code>null</code> is returned
	 * instead.
	 * 
	 * @param line
	 *            A single line in CSV format representing a UDC event.
	 * @return An instance of {@link UsageDataEvent} containing the information
	 *         found in the given string.
	 */
	private UsageDataEvent createUsageDataEvent(String line) {
		String[] tokens = UsageDataRecorderUtils.splitLine(line);
		if (tokens == null) return null;
		if (tokens.length != 6) return null;
		Long when;
		try {
			when = Long.valueOf(tokens[5].trim());
		} catch (NumberFormatException e) {
			return null; // How's that for error recovery?
		}
		UsageDataEvent usageDataEvent = new UsageDataEvent(tokens[0], tokens[1], tokens[4], tokens[2], tokens[3], when);
		return usageDataEvent;
	}

	public void close() throws IOException {
		reader.close();
	}

	/**
	 * This method provides a mechanism for visiting the contents of
	 * a file containing UDC data.
	 * 
	 * @see #iterate(IProgressMonitor, Iterator)
	 * 
	 * @param iterator instance of {@link Iterator} to notify.
	 * @throws Exception
	 */
	public void iterate(Iterator iterator) throws Exception {
		iterate(new NullProgressMonitor(), iterator);
	}

	/**
	 * This method provides a mechanism for visiting the contents of
	 * a file containing UDC data. Essentially, it implements a visitor
	 * pattern (and in retrospect, we probably should have called this
	 * method &quot;visit&quot; or something equally clever). The 
	 * {@link Iterator} is sent a separate message for the header, and
	 * then for each line in the file that we're processing.
	 * 
	 * <p>Lines in the file that cause errors on parsing attempts
	 * are skipped.</p>
	 * 
	 * @see #iterate(IProgressMonitor, Iterator)
	 * 
	 * @param monitor a progress monitor
	 * @param iterator instance of {@link Iterator} to notify.
	 * @throws Exception
	 */
	public void iterate(IProgressMonitor monitor, Iterator iterator) throws Exception {
		monitor.beginTask("Iterate over usage data file", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
		try {
			// The first line is the header.
			iterator.header(reader.readLine());
			while (true) {
				if (monitor.isCanceled()) break;
				String line = reader.readLine();
				if (line == null) break;
				UsageDataEvent event = createUsageDataEvent(line);
				if (event != null) iterator.event(line, event);
			}
		} finally {
			monitor.done();
		}
	}

}
