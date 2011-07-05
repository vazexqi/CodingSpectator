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
package org.eclipse.epp.usagedata.internal.recording.filtering;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.IPropertyChangeListener;

public abstract class AbstractUsageDataEventFilter implements UsageDataEventFilter {

	ListenerList changeListeners = new ListenerList();
	IPropertyChangeListener propertyChangeListener;


	public void addFilterChangeListener(FilterChangeListener filterChangeListener) {
		changeListeners.add(filterChangeListener);
	}

	public void removeFilterChangeListener(FilterChangeListener filterChangeListener) {
		changeListeners.remove(filterChangeListener);
	}

	protected void fireFilterChangedEvent() {
		for (Object listener : changeListeners.getListeners()) {
			((FilterChangeListener)listener).filterChanged();
		}
	}

	protected boolean matches(String pattern, String bundleId) {
		return bundleId.matches(asRegex(pattern));
	}

	String asRegex(String filter) {
		StringBuilder builder = new StringBuilder();
		for(int index=0;index<filter.length();index++) {
			char next = filter.charAt(index);
			if (next == '*') builder.append(".*"); //$NON-NLS-1$
			else if (next == '.') builder.append("\\."); //$NON-NLS-1$
			else builder.append(next);
		}
		return builder.toString();
	}

}