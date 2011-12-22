/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.core.submission;

/**
 * 
 * Note that {@link #postSubmit(boolean)} can be called without preceding {@link #preSubmit()}
 * and/or {@link #preCommit()} calls.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * @author Stas Negara
 * 
 */
public interface SubmitterListener {

	public void preSubmit();

	public void preCommit();

	public void postSubmit(boolean succeeded);

}
