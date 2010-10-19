/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker;

import java.util.Set;

import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.EditorPart;

/**
 * 
 * @author Stas Negara
 * @author Mohsen Vakilian - Extracted this class from CodeChangeTracker
 * 
 */
public class UserSessionState {
	private IFile currentFile;

	private EditorPart currentEditor;

	private ISourceViewer listenedViewer;

	private Set<CompareEditor> openConflictEditors;

	private Set<CompareEditor> dirtyConflictEditors;

	private boolean isRefactoring;

	private boolean isUndoing;

	private boolean isRedoing;

	private boolean isPartListenerRegistered;

	private IWorkbenchWindow activeWorkbenchWindow;

	private Set<IFile> dirtyFiles;

	public UserSessionState(IFile currentFile, EditorPart currentEditor, ISourceViewer listenedViewer, Set<CompareEditor> openConflictEditors, Set<CompareEditor> dirtyConflictEditors,
			boolean isRefactoring, boolean isUndoing, boolean isRedoing, boolean isPartListenerRegistered, IWorkbenchWindow activeWorkbenchWindow, Set<IFile> dirtyFiles) {
		this.currentFile= currentFile;
		this.currentEditor= currentEditor;
		this.listenedViewer= listenedViewer;
		this.openConflictEditors= openConflictEditors;
		this.dirtyConflictEditors= dirtyConflictEditors;
		this.isRefactoring= isRefactoring;
		this.isUndoing= isUndoing;
		this.isRedoing= isRedoing;
		this.isPartListenerRegistered= isPartListenerRegistered;
		this.activeWorkbenchWindow= activeWorkbenchWindow;
		this.dirtyFiles= dirtyFiles;
	}

	public IFile getCurrentFile() {
		return currentFile;
	}

	public void setCurrentFile(IFile currentFile) {
		this.currentFile= currentFile;
	}

	public EditorPart getCurrentEditor() {
		return currentEditor;
	}

	public void setCurrentEditor(EditorPart currentEditor) {
		this.currentEditor= currentEditor;
	}

	public ISourceViewer getListenedViewer() {
		return listenedViewer;
	}

	public void setListenedViewer(ISourceViewer listenedViewer) {
		this.listenedViewer= listenedViewer;
	}

	public Set<CompareEditor> getOpenConflictEditors() {
		return openConflictEditors;
	}

	public void setOpenConflictEditors(Set<CompareEditor> openConflictEditors) {
		this.openConflictEditors= openConflictEditors;
	}

	public Set<CompareEditor> getDirtyConflictEditors() {
		return dirtyConflictEditors;
	}

	public void setDirtyConflictEditors(Set<CompareEditor> dirtyConflictEditors) {
		this.dirtyConflictEditors= dirtyConflictEditors;
	}

	public boolean isRefactoring() {
		return isRefactoring;
	}

	public void setRefactoring(boolean isRefactoring) {
		this.isRefactoring= isRefactoring;
	}

	public boolean isUndoing() {
		return isUndoing;
	}

	public void setUndoing(boolean isUndoing) {
		this.isUndoing= isUndoing;
	}

	public boolean isRedoing() {
		return isRedoing;
	}

	public void setRedoing(boolean isRedoing) {
		this.isRedoing= isRedoing;
	}

	public boolean isPartListenerRegistered() {
		return isPartListenerRegistered;
	}

	public void setPartListenerRegistered(boolean isPartListenerRegistered) {
		this.isPartListenerRegistered= isPartListenerRegistered;
	}

	public IWorkbenchWindow getActiveWorkbenchWindow() {
		return activeWorkbenchWindow;
	}

	public void setActiveWorkbenchWindow(IWorkbenchWindow activeWorkbenchWindow) {
		this.activeWorkbenchWindow= activeWorkbenchWindow;
	}

	public Set<IFile> getDirtyFiles() {
		return dirtyFiles;
	}

	public void setDirtyFiles(Set<IFile> dirtyFiles) {
		this.dirtyFiles= dirtyFiles;
	}
}
