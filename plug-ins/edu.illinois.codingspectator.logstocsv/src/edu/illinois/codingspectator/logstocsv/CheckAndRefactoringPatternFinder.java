/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.logstocsv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import edu.illinois.codingtracker.operations.junit.TestSessionStartedOperation;
import edu.illinois.codingtracker.operations.starts.LaunchedApplicationOperation;

/**
 * 
 * This class analyzes the patterns of checking refactorings.
 * 
 * @author Mohsen Vakilian
 * 
 */
public class CheckAndRefactoringPatternFinder {

	private Collection<Event> events;

	private String csvFileName;

	public CheckAndRefactoringPatternFinder(Collection<Event> events, String csvFileName) {
		this.events= events;
		this.csvFileName= csvFileName;
	}

	public void reportChecksAfterRefactorings() throws IOException {
		Collection<CheckAndRefactoring> checksAfterRefactorings= findChecksAfterRefactorings();
		new CodingSpectatorCSVWriter(csvFileName).writeToCSV(checksAfterRefactorings);
	}

	private Collection<CheckAndRefactoring> findChecksAfterRefactorings() {
		Collection<CheckAndRefactoring> checksAfterRefactorings= new ArrayList<CheckAndRefactoring>();
		ArrayList<Event> events= relevantEventsSortedByTimestamp();

		Event currentRefactoringEvent= null;
		Event currentCheckEvent= null;
		boolean foundCheckAfterRefactoring= false;

		for (int i= 0; i < events.size(); i++) {
			Event event= events.get(i);
			if (isRefactoring(event)) {
				currentRefactoringEvent= event;
				foundCheckAfterRefactoring= false;
			} else if (isCheck(event)) {
				currentCheckEvent= event;
				if (!foundCheckAfterRefactoring && isCheckAfterRefactoring(currentRefactoringEvent, currentCheckEvent)) {
					foundCheckAfterRefactoring= true;
					checksAfterRefactorings.add(new CheckAndRefactoring(currentRefactoringEvent, currentCheckEvent));
				}
			}
		}
		return checksAfterRefactorings;
	}

	private Collection<Event> relevantEvents() {
		Collection<Event> relevantEvents= new ArrayList<Event>();
		for (Event event : events) {
			if (isCheck(event) || isRefactoring(event)) {
				relevantEvents.add(event);
			}
		}
		return relevantEvents;
	}

	private ArrayList<Event> relevantEventsSortedByTimestamp() {
		Event[] eventsArray= relevantEvents().toArray(new Event[] {});
		Arrays.sort(eventsArray, new Comparator<Event>() {

			@Override
			public int compare(Event e1, Event e2) {
				return Long.signum(e1.getTimestamp() - e2.getTimestamp());
			}

		});
		return new ArrayList<Event>(Arrays.asList(eventsArray));
	}

	private boolean isRefactoring(Event event) {
		if (event == null) {
			return false;
		} else if (event instanceof UserOperationEvent) {
			return ((UserOperationEvent)event).isStartedPerformedRefactoringOperation();
		} else {
			return false;
		}
	}

	private boolean isCheck(Event event) {
		if (event == null) {
			return false;
		} else if (event instanceof UserOperationEvent) {
			UserOperationEvent userOperationEvent= (UserOperationEvent)event;
			return userOperationEvent.getDescription().equals(new TestSessionStartedOperation().getDescription())
					|| userOperationEvent.getDescription().equals(new LaunchedApplicationOperation().getDescription());
		} else {
			return false;
		}
	}

	private boolean isCheckAfterRefactoring(Event refactoring, Event check) {
		if (!isRefactoring(refactoring) || !isCheck(check)) {
			return false;
		}
		long threshhold= 5 * 60 * 1000; //5 minutes in milliseconds
		return refactoring.getTimestamp() <= check.getTimestamp() && check.getTimestamp() <= refactoring.getTimestamp() + threshhold;
	}

}
