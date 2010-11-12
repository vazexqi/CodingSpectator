/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring.history;

import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryListener;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistoryEvent;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;

/**
 * Refactoring history listener which continuously persists the global refactoring history in the
 * different history locations.
 * 
 * @author Mohsen Vakilian, nchen - Added the support for serializing three new types of events,
 *         i.e. canceled, performed and disallowed refactorings.
 * 
 * @since 3.2
 */
public final class RefactoringHistorySerializer implements IRefactoringHistoryListener {

	/**
	 * {@inheritDoc}
	 */
	public void historyNotification(final RefactoringHistoryEvent event) {
		Assert.isNotNull(event);
		serialize(event, getHistoryFolder(event));
	}

	private String getHistoryFolder(final RefactoringHistoryEvent event) {
		Assert.isNotNull(event);
		switch (event.getEventType()) {
			//CODINGSPECTATOR: Added the following two cases for handling the events that CodingSpectator is interested in.
			case RefactoringHistoryEvent.CODINGSPECTATOR_REFACTORING_CANCELED:
				return RefactoringHistoryService.getRefactoringHistoryCanceledFolder();

			case RefactoringHistoryEvent.CODINGSPECTATOR_REFACTORING_PERFORMED:
				return RefactoringHistoryService.getRefactoringHistoryPerformedFolder();
				
			case RefactoringHistoryEvent.CODINGSPECTATOR_REFACTORING_DISALLOWED:
				return RefactoringHistoryService.getRefactoringHistoryDisallowedFolder();

			case RefactoringHistoryEvent.ADDED:
			case RefactoringHistoryEvent.PUSHED:
			case RefactoringHistoryEvent.POPPED:
				return RefactoringHistoryService.NAME_HISTORY_FOLDER;
		}
		return null;
	}

	private void serialize(final RefactoringHistoryEvent event, String historyFolder) {
		final RefactoringDescriptorProxy proxy= event.getDescriptor();
		final long stamp= proxy.getTimeStamp();
		if (stamp >= 0) {
			final String name= proxy.getProject();

			IFileStore store= getFileStore(historyFolder);

			if (name != null && !"".equals(name)) { //$NON-NLS-1$
				final IProject project= ResourcesPlugin.getWorkspace().getRoot().getProject(name);
				if (project.isAccessible()) {
					if (RefactoringHistoryService.hasSharedRefactoringHistory(project)) {
						final URI uri= project.getLocationURI();
						if (uri != null) {
							try {
								processHistoryNotification(EFS.getStore(uri).getChild(RefactoringHistoryService.NAME_HISTORY_FOLDER), event, name);
							} catch (CoreException exception) {
								RefactoringCorePlugin.log(exception);
							} finally {
								try {
									project.refreshLocal(IResource.DEPTH_INFINITE, null);
								} catch (CoreException exception) {
									RefactoringCorePlugin.log(exception);
								}
							}
						}
					} else {
						try {
							processHistoryNotification(store.getChild(name), event, name);
						} catch (CoreException exception) {
							RefactoringCorePlugin.log(exception);
						}
					}
				}
			} else {
				try {
					processHistoryNotification(store.getChild(RefactoringHistoryService.NAME_WORKSPACE_PROJECT), event, name);
				} catch (CoreException exception) {
					RefactoringCorePlugin.log(exception);
				}
			}
		}
	}

	//CODINGSPECTATOR: Extracted the method getFileStore(String).
	private IFileStore getFileStore(String historyFolder) {
		return EFS.getLocalFileSystem().getStore(RefactoringCorePlugin.getDefault().getStateLocation()).getChild(historyFolder);
	}

	/**
	 * Processes the history event.
	 * 
	 * @param store the file store
	 * @param event the history event
	 * @param name the project name, or <code>null</code>
	 * @throws CoreException if an error occurs
	 */
	private void processHistoryNotification(final IFileStore store, final RefactoringHistoryEvent event, final String name) throws CoreException {
		final RefactoringDescriptorProxy proxy= event.getDescriptor();
		final int type= event.getEventType();
		final RefactoringHistoryManager manager= new RefactoringHistoryManager(store, name);
		final NullProgressMonitor monitor= new NullProgressMonitor();
		if (isInsertion(type)) {
			final RefactoringDescriptor descriptor= proxy.requestDescriptor(monitor);
			if (descriptor != null)
				manager.addRefactoringDescriptor(descriptor, type == RefactoringHistoryEvent.ADDED, monitor);
		} else
			manager.removeRefactoringDescriptors(new RefactoringDescriptorProxy[] { proxy }, monitor, RefactoringCoreMessages.RefactoringHistoryService_updating_history);
	}

	private boolean isInsertion(final int type) {
		//CODINGSPECTATOR: Made the two events of CodingSpectator get inserted to the history file.
		if (type == RefactoringHistoryEvent.PUSHED || type == RefactoringHistoryEvent.ADDED || type == RefactoringHistoryEvent.CODINGSPECTATOR_REFACTORING_CANCELED
				|| type == RefactoringHistoryEvent.CODINGSPECTATOR_REFACTORING_PERFORMED || type == RefactoringHistoryEvent.CODINGSPECTATOR_REFACTORING_DISALLOWED)
			return true;
		else if (type == RefactoringHistoryEvent.POPPED)
			return false;
		else
			throw new IllegalArgumentException();
	}
}
