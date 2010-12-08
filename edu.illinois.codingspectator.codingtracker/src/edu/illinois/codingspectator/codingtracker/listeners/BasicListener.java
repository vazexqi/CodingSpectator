/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.listeners;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.EditorPart;

import edu.illinois.codingspectator.codingtracker.recording.EventRecorder;

/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public abstract class BasicListener {

	protected static final EventRecorder eventRecorder= EventRecorder.getInstance();

	protected static volatile IWorkbenchWindow activeWorkbenchWindow= null;

	protected static volatile IFile currentFile= null;

	protected static volatile EditorPart currentEditor= null;

	protected static final Set<CompareEditor> openConflictEditors= Collections.synchronizedSet(new HashSet<CompareEditor>());

	protected static final Set<CompareEditor> dirtyConflictEditors= Collections.synchronizedSet(new HashSet<CompareEditor>());

	protected static final Set<IFile> dirtyFiles= Collections.synchronizedSet(new HashSet<IFile>());

	protected static volatile boolean isRefactoring= false;

	protected static volatile boolean isUndoing= false;

	protected static volatile boolean isRedoing= false;

}
