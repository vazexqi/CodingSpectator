/*******************************************************************************
 * Copyright (c) 2008 The Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    The Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.epp.usagedata.internal.gathering.monitors;

import org.eclipse.epp.usagedata.internal.gathering.services.UsageDataService;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Instances of the {@link PartUsageMonitor} class monitor the use of parts in
 * the workbench. More specifically, it is notified whenever a view or editor is
 * opened, closed, activated (given focus), etc. and sends some of these events
 * to the UsageDataService. In the spirit of doing far too much in one place,
 * instances also monitor opening, closing, activation, and deactivation of
 * workbench windows. While we're at it, instances also monitor the activation
 * of perspectives.
 * <p>
 * When sent the {@link #startMonitoring(UsageDataService)} message, an instance
 * adds several listeners to various elements of the workbench. Those listeners
 * are removed when the instance is sent {@link #stopMonitoring()}. The
 * listeners either record events, or add/remove listeners to windows and pages
 * as they are opened/closed.
 * </p>
 * 
 * @author Wayne Beaton
 * 
 */
public class PartUsageMonitor implements UsageMonitor {
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private static final String WORKBENCH_BUNDLE_ID = "org.eclipse.ui.workbench"; //$NON-NLS-1$
	private static final String PERSPECTIVES_EXTENSION_POINT = "org.eclipse.ui.perspectives"; //$NON-NLS-1$

	private static final String WORKBENCH = "workbench"; //$NON-NLS-1$
	private static final String PERSPECTIVE = "perspective"; //$NON-NLS-1$
	private static final String DEACTIVATED = "deactivated"; //$NON-NLS-1$
	private static final String ACTIVATED = "activated"; //$NON-NLS-1$
	private static final String CLOSED = "closed"; //$NON-NLS-1$
	private static final String OPENED = "opened"; //$NON-NLS-1$
	private static final String PART = "part"; //$NON-NLS-1$
	private static final String VIEW = "view"; //$NON-NLS-1$
	private static final String EDITOR = "editor"; //$NON-NLS-1$

	private UsageDataService usageDataService;
	
	private IWindowListener windowListener = new IWindowListener() {
		public void windowOpened(IWorkbenchWindow window) {
			recordEvent(OPENED, window);
			hookListener(window);
		}

		public void windowClosed(IWorkbenchWindow window) {
			recordEvent(CLOSED, window);
			unhookListeners(window);
		}

		public void windowActivated(IWorkbenchWindow window) {
			recordEvent(ACTIVATED, window);
		}

		public void windowDeactivated(IWorkbenchWindow window) {
			recordEvent(DEACTIVATED, window);
		}

	};
	
	private IPageListener pageListener = new IPageListener() {
		public void pageActivated(IWorkbenchPage page) {
		}

		public void pageClosed(IWorkbenchPage page) {
			unhookListeners(page);			
		}

		public void pageOpened(IWorkbenchPage page) {
			hookListeners(page);			
		}
		
	};
	
	private IPartListener partListener = new IPartListener() {
		public void partActivated(IWorkbenchPart part) {
			recordEvent(ACTIVATED, part);
		}

		public void partDeactivated(IWorkbenchPart part) {
			// Don't care.
		}

		public void partBroughtToTop(IWorkbenchPart part) {
			// Don't care.
		}

		public void partClosed(IWorkbenchPart part) {
			recordEvent(CLOSED, part);
		}

		public void partOpened(IWorkbenchPart part) {
			recordEvent(OPENED, part);
		}
	};

	private IPerspectiveListener perspectiveListener = new IPerspectiveListener() {
		public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
			recordEvent(ACTIVATED, perspective);
		}

		public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {			
		}
		
	};

	private ExtensionIdToBundleMapper perspectiveToBundleIdMapper;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.epp.usagedata.internal.gathering.UsageMonitor#register(org.eclipse.epp.usagedata.internal.gathering.UsageDataService)
	 */
	public void startMonitoring(UsageDataService usageDataService) {
		this.usageDataService = usageDataService;
		IWorkbench workbench = PlatformUI.getWorkbench();		
		perspectiveToBundleIdMapper = new ExtensionIdToBundleMapper(PERSPECTIVES_EXTENSION_POINT);
		hookListeners(workbench);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.epp.usagedata.internal.gathering.UsageMonitor#deregister()
	 */
	public void stopMonitoring() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		unhookListeners(workbench);
		perspectiveToBundleIdMapper.dispose();
	}

	/**
	 * This method hooks a part listener to all currently open
	 * workbench windows.
	 * 
	 * @param workbench
	 */
	private void hookListeners(final IWorkbench workbench) {
		workbench.addWindowListener(windowListener);
		/*
		 * The syncExec code is no longer required. Previously,
		 * we were only applying the listeners to the active workbench window and
		 * Workbench#getActiveWorkbenchWindow() must be called in the ui thread.
		 */
//		workbench.getDisplay().syncExec(new Runnable() {
//			public void run() {
				for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
					hookListener(window);
				}
//			}
//		});
	}

	private void unhookListeners(final IWorkbench workbench) {
		// If the display is disposed, then we're shutting down and the
		// listeners have already been removed.
		if (workbench.getDisplay().isDisposed())
			return;

		workbench.removeWindowListener(windowListener);
		
		// Walk through the workbench windows and unhook the listeners from each
		// of them.
//		workbench.getDisplay().syncExec(new Runnable() {
//			public void run() {
				for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
					unhookListeners(window);
				}
//			}
//		});
	}
	
	private void hookListener(IWorkbenchWindow window) {
		if (window == null) return;
		window.addPageListener(pageListener);
		window.addPerspectiveListener(perspectiveListener);
		for (IWorkbenchPage page : window.getPages()) {
			hookListeners(page);
		}
	}

	private void unhookListeners(IWorkbenchWindow window) {
		if (window == null) return;
		window.removePageListener(pageListener);
		window.removePerspectiveListener(perspectiveListener);
		for(IWorkbenchPage page : window.getPages()) {
			unhookListeners(page);
		}
	}
	
	private void hookListeners(IWorkbenchPage page) {
		IPerspectiveDescriptor perspective = page.getPerspective();
		if (perspective != null) {
			recordEvent(ACTIVATED, perspective);
		}
		page.addPartListener(partListener);
	}

	private void unhookListeners(IWorkbenchPage page) {
		page.removePartListener(partListener);
	}
	
	protected void recordEvent(String event, IWorkbenchWindow window) {
		// TODO Hardcoding bundle id for now.
		// TODO Does an IWorkbenchWindow have an id?
		usageDataService.recordEvent(event, WORKBENCH, EMPTY_STRING, WORKBENCH_BUNDLE_ID);
	}

	protected void recordEvent(String event, IPerspectiveDescriptor perspective) {
		String id = perspective.getId();
		usageDataService.recordEvent(event, PERSPECTIVE, id, perspectiveToBundleIdMapper.getBundleId(id));
	}
	
	private void recordEvent(String event, IWorkbenchPart part) {
		IWorkbenchPartSite site = part.getSite();
		usageDataService.recordEvent(event, getKind(site), site.getId(), site.getPluginId());
	}

	/**
	 * This method returns the &quot;kind&quot; of thing that's represented by
	 * <code>site</code>. More specifically, this method answers the
	 * extension point from which the thing represented by <code>site</code>
	 * is defined. Should be an editor or view. Answers <code>null</code> if
	 * the &quot;kind&quot; cannot be determined.
	 * 
	 * @param site
	 * @return Name of the extension point from which the editor or view is
	 *         created, or null if it cannot be determined.
	 */
	private String getKind(IWorkbenchPartSite site) {
		if (site instanceof IEditorSite)
			return EDITOR;
		else if (site instanceof IViewSite)
			return VIEW;
		return PART;
	}
}