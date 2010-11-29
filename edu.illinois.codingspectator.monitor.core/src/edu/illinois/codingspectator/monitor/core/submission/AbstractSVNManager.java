/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.core.submission;

import java.io.File;

/**
 * This is the implementation of the {@link Submitter} design contract for an SVN backend.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public abstract class AbstractSVNManager {

	protected final File svnWorkingCopyDirectory;

	protected AbstractSVNManager(String svnWorkingCopyDirectory) {
		this.svnWorkingCopyDirectory= new File(svnWorkingCopyDirectory);
	}

}
