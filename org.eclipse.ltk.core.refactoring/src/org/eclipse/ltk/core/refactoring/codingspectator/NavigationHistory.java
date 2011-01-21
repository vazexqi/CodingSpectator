package org.eclipse.ltk.core.refactoring.codingspectator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Mohsen Vakilian
 * @author nchen
 */
public class NavigationHistory {

	final List navigationHistoryItems;

	public NavigationHistory() {
		navigationHistoryItems= new ArrayList();
	}

	public void addItem(NavigationHistoryItem item) {
		navigationHistoryItems.add(item);
	}

	public String toString() {
		StringBuilder builder= new StringBuilder("{"); //$NON-NLS-1$

		for (Iterator iterator= navigationHistoryItems.iterator(); iterator.hasNext();) {
			NavigationHistoryItem item= (NavigationHistoryItem)iterator.next();
			builder.append(item);
			builder.append(","); //$NON-NLS-1$
		}

		builder.append("}"); //$NON-NLS-1$

		return builder.toString();
	}

}
