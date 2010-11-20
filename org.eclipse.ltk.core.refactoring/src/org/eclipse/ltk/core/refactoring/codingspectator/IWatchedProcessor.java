package org.eclipse.ltk.core.refactoring.codingspectator;


/**
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public interface IWatchedProcessor extends IWatched {

	public String getSelection();

	public String getDescriptorID();

	public String getJavaProjectName();

}
