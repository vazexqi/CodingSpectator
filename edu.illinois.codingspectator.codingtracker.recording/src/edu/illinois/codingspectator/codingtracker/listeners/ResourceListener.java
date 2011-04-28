/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.listeners;

import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.core.internal.resources.IResourceListener;
import org.eclipse.core.internal.resources.Resource;
import org.eclipse.core.resources.IFile;


/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public class ResourceListener extends BasicListener implements IResourceListener {

	private boolean lastSavedFileSuccess= false;

	private boolean isSavingCompareEditor= false;

	public static void register() {
		Resource.resourceListener= new ResourceListener();
	}

	@Override
	public void savedFile(IFile file, boolean success) {
		if ("java".equals(file.getFileExtension())) {
			lastSavedFileSuccess= success;
			if (!isSavingCompareEditor) { //compare editor saving is handled in a different method
				operationRecorder.recordSavedFile(file, success);
			}
		}
	}

	@Override
	public void aboutToSaveCompareEditor(Object compareEditor) {
		isSavingCompareEditor= true;
		lastSavedFileSuccess= false;
	}

	@Override
	public void savedCompareEditor(Object compareEditor) {
		isSavingCompareEditor= false;
		operationRecorder.recordSavedCompareEditor((CompareEditor)compareEditor, lastSavedFileSuccess);
	}

}
