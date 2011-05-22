package org.eclipse.ltk.core.refactoring.codingspectator;

/**
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public interface IWatchedProcessor extends IWatched {

	public CodeSnippetInformation getCodeSnippetInformation();

	public String getDescriptorID();

	public String getJavaProjectName();

}
