/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.listeners;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import edu.illinois.codingtracker.recording.Activator;
import edu.illinois.codingtracker.recording.KnownFilesRecorder;
import edu.illinois.codingtracker.recording.OperationRecorder;
import edu.illinois.codingtracker.recording.ast.ASTOperationRecorder;

/**
 * 
 * @author Stas Negara
 * @author Mohsen Vakilian - Added the method {@link #getActiveWorkbenchWindow}.
 * 
 */
@SuppressWarnings("restriction")
public abstract class BasicListener {

	protected static final KnownFilesRecorder knownFilesRecorder= KnownFilesRecorder.getInstance();

	protected static final OperationRecorder operationRecorder= OperationRecorder.getInstance();

	protected static final ASTOperationRecorder astRecorder= ASTOperationRecorder.getInstance();

	protected static final Set<CompareEditor> openConflictEditors= Collections.synchronizedSet(new HashSet<CompareEditor>());

	protected static volatile boolean isRefactoring= false;

	//It is public such that online AST inferencing can detect that particular changes are caused by undoing code edits.
	public static volatile boolean isUndoing= false;

	protected static volatile boolean isRedoing= false;

	protected static volatile boolean isBufferContentAboutToBeReplaced= false;

	protected static IWorkbenchWindow getActiveWorkbenchWindow() {
		IWorkbench workbench= null;
		try {
			workbench= PlatformUI.getWorkbench();
		} catch (IllegalStateException e) {
			Activator.getDefault().log(Activator.createErrorStatus("Workbench has not been created yet.", e));
			return null;
		}
		IWorkbenchWindow activeWorkbenchWindow= null;
		if (workbench != null) {
			activeWorkbenchWindow= workbench.getActiveWorkbenchWindow();
		}
		return activeWorkbenchWindow;
	}

}
