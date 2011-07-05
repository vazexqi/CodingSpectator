/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.errorlog;

import java.io.StringWriter;
import java.io.Writer;

import org.eclipse.core.internal.runtime.PlatformLogWriter;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.internal.adaptor.EclipseLogWriter;
import org.eclipse.equinox.log.ExtendedLogEntry;
import org.eclipse.osgi.framework.log.FrameworkLogEntry;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

import edu.illinois.codingspectator.saferecorder.SafeRecorder;

/**
 * 
 * @author Mohsen Vakilian
 * @author Balaji Ambresh Rajkumar
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public class ErrorLogListener implements ILogListener {

	private static final SafeRecorder safeRecorder= new SafeRecorder("error.log");

	public synchronized void logging(IStatus status, String plugin) {
		final FrameworkLogEntry frameworkLogEntry= PlatformLogWriter.getLog(status);
		Writer writer= new StringWriter();
		new EclipseLogWriter(writer, "CodingSpectatorErrorLog", true).logged(new ExtendedLogEntry() {

			@Override
			public long getTime() {
				throw new UnsupportedOperationException();
			}

			@SuppressWarnings("rawtypes")
			@Override
			public ServiceReference getServiceReference() {
				throw new UnsupportedOperationException();
			}

			@Override
			public String getMessage() {
				throw new UnsupportedOperationException();
			}

			@Override
			public int getLevel() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Throwable getException() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Bundle getBundle() {
				throw new UnsupportedOperationException();
			}

			@Override
			public String getThreadName() {
				throw new UnsupportedOperationException();
			}

			@Override
			public long getThreadId() {
				throw new UnsupportedOperationException();
			}

			@Override
			public long getSequenceNumber() {
				throw new UnsupportedOperationException();
			}

			@Override
			public String getLoggerName() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Object getContext() {
				return frameworkLogEntry;
			}
		});
		safeRecorder.record(writer.toString());
	}

}
