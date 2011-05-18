/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.listeners;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.ui.IWorkbenchWindow;

import edu.illinois.codingtracker.recording.KnownFilesRecorder;
import edu.illinois.codingtracker.recording.OperationRecorder;

/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public abstract class BasicListener {

	protected static final KnownFilesRecorder knownFilesRecorder= KnownFilesRecorder.getInstance();

	protected static final OperationRecorder operationRecorder= OperationRecorder.getInstance();

	protected static volatile IWorkbenchWindow activeWorkbenchWindow= null;

	protected static final Set<CompareEditor> openConflictEditors= Collections.synchronizedSet(new HashSet<CompareEditor>());

	protected static final Set<CompareEditor> dirtyConflictEditors= Collections.synchronizedSet(new HashSet<CompareEditor>());

	protected static volatile boolean isRefactoring= false;

	protected static volatile boolean isUndoing= false;

	protected static volatile boolean isRedoing= false;

	protected static volatile boolean isBufferContentAboutToBeReplaced= false;

}
