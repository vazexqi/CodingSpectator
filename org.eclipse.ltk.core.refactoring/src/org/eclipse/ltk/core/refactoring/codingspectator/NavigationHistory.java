package org.eclipse.ltk.core.refactoring.codingspectator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Mohsen Vakilian
 * @author nchen
 */
public class NavigationHistory {
	private static final String INITIAL_DIALOG_MARKER= "BEGIN"; //$NON-NLS-1$

	final List navigationHistoryItems;

	public NavigationHistory(String buttonLabel, long initialTimestamp) {
		navigationHistoryItems= new ArrayList();
		navigationHistoryItems.add(new NavigationHistoryItem(INITIAL_DIALOG_MARKER, buttonLabel, initialTimestamp));
	}

	public void addItem(NavigationHistoryItem item) {
		navigationHistoryItems.add(item);
	}

	public String toString() {
		StringBuilder builder= new StringBuilder("{ "); //$NON-NLS-1$

		for (Iterator iterator= navigationHistoryItems.iterator(); iterator.hasNext();) {
			NavigationHistoryItem item= (NavigationHistoryItem)iterator.next();
			builder.append(item);
			builder.append(" "); //$NON-NLS-1$
		}

		builder.append(" }"); //$NON-NLS-1$

		return builder.toString();
	}

}
