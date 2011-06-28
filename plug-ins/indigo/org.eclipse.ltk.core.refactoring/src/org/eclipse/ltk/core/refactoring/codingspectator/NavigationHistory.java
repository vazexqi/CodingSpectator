package org.eclipse.ltk.core.refactoring.codingspectator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Mohsen Vakilian
 * @author nchen
 * @author Balaji Ambresh Rajkumar
 * 
 */
public class NavigationHistory {

	final List navigationHistoryItems;

	public NavigationHistory() {
		navigationHistoryItems= new ArrayList();
	}

	public void addItem(NavigationHistoryItem item) {
		navigationHistoryItems.add(item);
	}

	public List getNavigationHistoryItems() {
		return Collections.unmodifiableList(navigationHistoryItems);
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

	public static NavigationHistory parse(String navigationHistoryString) throws ParseException {
		NavigationHistory navigationHistory= new NavigationHistory();
		int head= 0;
		while (true) {
			int indexOfBeginMarker= navigationHistoryString.indexOf(NavigationHistoryItem.BEGIN_MARKER, head);
			if (indexOfBeginMarker == -1)
				break;
			head= indexOfBeginMarker + 1;
			int indexOfEndMarker= navigationHistoryString.indexOf(NavigationHistoryItem.END_MARKER, head);
			if (indexOfEndMarker == -1) {
				throw new ParseException("Expected " + NavigationHistoryItem.END_MARKER);
			}
			head= indexOfEndMarker + 1;
			navigationHistory.addItem(NavigationHistoryItem.parse(navigationHistoryString.substring(indexOfBeginMarker, indexOfEndMarker + 1)));
		}
		return navigationHistory;
	}

	public static class ParseException extends Exception {

		public ParseException(String message) {
			super(message);
		}

	}
}
