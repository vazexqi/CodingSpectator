/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.ltk.core.refactoring.codingspectator.Logger;
import org.eclipse.ltk.core.refactoring.codingspectator.NavigationHistory;
import org.eclipse.ltk.core.refactoring.codingspectator.NavigationHistory.ParseException;
import org.eclipse.ltk.core.refactoring.codingspectator.NavigationHistoryItem;

/**
 * @author Mohsen Vakilian
 * @author Balaji Ambresh Rajkumar
 * 
 */
public class DescriptorComparator {

	public static void assertMatches(CapturedRefactoringDescriptor expectedRefactoringDescriptor, CapturedRefactoringDescriptor actualRefactoringDescriptor) {
		assertEquals(expectedRefactoringDescriptor.getComment(), actualRefactoringDescriptor.getComment());
		assertEquals(expectedRefactoringDescriptor.getDescription(), actualRefactoringDescriptor.getDescription());
		assertEquals(expectedRefactoringDescriptor.getFlags(), actualRefactoringDescriptor.getFlags());
		assertEquals(expectedRefactoringDescriptor.getID(), actualRefactoringDescriptor.getID());
		assertEquals(expectedRefactoringDescriptor.getProject(), actualRefactoringDescriptor.getProject());
		assertTrue(actualRefactoringDescriptor.getTimestamp() > 0);

		Set<String> expectedAttributeKeys= expectedRefactoringDescriptor.getAttributeKeys();
		assertEquals(expectedAttributeKeys, actualRefactoringDescriptor.getAttributeKeys());

		HashSet<String> attributesWithoutTimestamps= new HashSet<String>(expectedAttributeKeys);
		attributesWithoutTimestamps.removeAll(Arrays.asList(Logger.NAVIGATION_HISTORY_ATTRIBUTE));

		for (String attribute : attributesWithoutTimestamps) {
			assertEquals(String.format("Expected another value for the attribute \"%s\"", attribute), expectedRefactoringDescriptor.getAttribute(attribute),
					actualRefactoringDescriptor.getAttribute(attribute));
		}

		if (expectedAttributeKeys.contains(Logger.NAVIGATION_HISTORY_ATTRIBUTE)) {
			try {
				assertEquals(getNavigationHistoryWithoutTimestamps(expectedRefactoringDescriptor), getNavigationHistoryWithoutTimestamps(actualRefactoringDescriptor));
			} catch (ParseException e) {
				throw new AssertionError(String.format("Failed to parse the value of the %s attribute.\n%s", Logger.NAVIGATION_HISTORY_ATTRIBUTE, e.getMessage()));
			}
		}
	}

	private static String getNavigationHistoryWithoutTimestamps(CapturedRefactoringDescriptor refactoringDescriptor) throws ParseException {
		return getNavigationHistoryWithoutTimestamps(refactoringDescriptor.getAttribute(Logger.NAVIGATION_HISTORY_ATTRIBUTE));
	}

	private static String getNavigationHistoryWithoutTimestamps(String navigationHistory) throws ParseException {
		return getNavigationHistoryWithoutTimestamps(NavigationHistory.parse(navigationHistory)).toString();
	}

	private static NavigationHistory getNavigationHistoryWithoutTimestamps(NavigationHistory navigationHistory) {
		NavigationHistory navigationHistoryWithoutTimestamps= new NavigationHistory();
		for (Object navigationHistoryItem : navigationHistory.getNavigationHistoryItems()) {
			navigationHistoryWithoutTimestamps.addItem(getNavigationHistoryItemWithoutTimestamps((NavigationHistoryItem)navigationHistoryItem));
		}
		return navigationHistoryWithoutTimestamps;
	}

	private static NavigationHistoryItem getNavigationHistoryItemWithoutTimestamps(NavigationHistoryItem navigationHistoryItem) {
		return new NavigationHistoryItem(navigationHistoryItem.getDialogID(), navigationHistoryItem.getButtonLabel(), -1);
	}

}
