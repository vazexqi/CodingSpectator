package org.eclipse.ltk.core.refactoring.codingspectator;



/**
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public interface IWatchedProcessor extends IWatched {

	/**
	 * @deprecated
	 */
	public String getSelection();

	public String getDescriptorID();

	public String getJavaProjectName();

	/**
	 * @see org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor#getElements()
	 */
	public Object[] getElements();

}
