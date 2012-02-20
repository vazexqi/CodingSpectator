/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.logstocsv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import edu.illinois.codingspectator.refactorings.parser.RefactoringLog;

/**
 * 
 * This class matches up the performed refactorings recorded by CodingSpectator and CodingTracker to
 * find the inconsistencies.
 * 
 * @author Mohsen Vakilian
 * 
 */
public class PerformedRefactoringMatcher {

	private Collection<Event> events;

	private String csvFileName;

	public PerformedRefactoringMatcher(Collection<Event> events, String csvFileName) {
		this.events= events;
		this.csvFileName= csvFileName;
	}

	public void reportMatchedPerformedRefactorings() throws IOException {
		Collection<MatchedPerformedRefactorings> matchedPerformedRefactorings= matchPerformedRefactorings();
		new CodingSpectatorCSVWriter(csvFileName).writeToCSV(matchedPerformedRefactorings);
	}

	private Collection<MatchedPerformedRefactorings> matchPerformedRefactorings() {
		ArrayList<Event> sortedCodingTrackerPerformedRefactorings= sortedByTimestamp(getCodingTrackerPerformedRefactorings());
		ArrayList<Event> sortedCodingSpectatorPerformedRefactorings= sortedByTimestamp(getCodingSpectatorPerformedRefactorings());
		ArrayList<Event> remainingSortedCodingSpectatorPerformedRefactorings= new ArrayList<Event>();
		Collection<MatchedPerformedRefactorings> matchedPerformedRefactorings= new HashSet<MatchedPerformedRefactorings>();
		for (Event event : sortedCodingSpectatorPerformedRefactorings) {
			RefactoringEvent csEvent= (RefactoringEvent)event;
			int index= findClosestMatchingEvent(sortedCodingTrackerPerformedRefactorings, event);
			if (index >= 0) {
				matchedPerformedRefactorings.add(MatchedPerformedRefactorings.createMatchedPerformedRefactorings(csEvent, sortedCodingTrackerPerformedRefactorings.get(index).getTimestamp()));
				sortedCodingTrackerPerformedRefactorings.remove(index);
			} else {
				matchedPerformedRefactorings.add(MatchedPerformedRefactorings.createMatchedPerformedRefactorings(csEvent, -1));
				remainingSortedCodingSpectatorPerformedRefactorings.add(csEvent);
			}
		}
		for (Event event : sortedCodingTrackerPerformedRefactorings) {
			UserOperationEvent ctEvent= (UserOperationEvent)event;
			int index= findClosestMatchingEvent(remainingSortedCodingSpectatorPerformedRefactorings, event);
			if (index >= 0) {
				matchedPerformedRefactorings.add(MatchedPerformedRefactorings
						.createMatchedPerformedRefactorings(ctEvent, remainingSortedCodingSpectatorPerformedRefactorings.get(index).getTimestamp()));
				remainingSortedCodingSpectatorPerformedRefactorings.remove(index);
			} else {
				matchedPerformedRefactorings.add(MatchedPerformedRefactorings.createMatchedPerformedRefactorings(ctEvent, -1));
			}
		}

		return sorted(matchedPerformedRefactorings);
	}

	private int findClosestMatchingEvent(ArrayList<Event> sortedEvents, Event event) {
		final long MAX_TIMESTAMP_DIFFERENCE= 5 * 60 * 1000; // 5 minutes in milliseconds
		long timestampDifference= 500;
		int index= -1;
		do {
			index= Collections.binarySearch(sortedEvents, event, getEventTimestampComparatorForFinding(timestampDifference));
			timestampDifference+= 500;
		} while (index < 0 && timestampDifference < MAX_TIMESTAMP_DIFFERENCE);
		return index;
	}

	private ArrayList<MatchedPerformedRefactorings> sorted(Collection<MatchedPerformedRefactorings> matched) {
		MatchedPerformedRefactorings[] matchedArray= matched.toArray(new MatchedPerformedRefactorings[] {});
		Arrays.sort(matchedArray);
		return new ArrayList<MatchedPerformedRefactorings>(Arrays.asList(matchedArray));
	}

	private boolean isCodingSpectatorPerformedRefactoring(Event event) {
		if (event == null) {
			return false;
		} else if (event instanceof RefactoringEvent) {
			return RefactoringLog.LogType.PERFORMED == ((RefactoringEvent)event).getRefactoringKind();
		} else {
			return false;
		}
	}

	private boolean isCodingTrackerPerformedRefactoring(Event event) {
		if (event == null) {
			return false;
		} else if (event instanceof UserOperationEvent) {
			return ((UserOperationEvent)event).isStartedPerformedRefactoringOperation();
		} else {
			return false;
		}
	}

	private Collection<Event> getCodingTrackerPerformedRefactorings() {
		Collection<Event> collectedEvents= new ArrayList<Event>();
		for (Event event : events) {
			if (isCodingTrackerPerformedRefactoring(event)) {
				collectedEvents.add(event);
			}
		}
		return collectedEvents;
	}

	private Collection<Event> getCodingSpectatorPerformedRefactorings() {
		Collection<Event> collectedEvents= new ArrayList<Event>();
		for (Event event : events) {
			if (isCodingSpectatorPerformedRefactoring(event)) {
				collectedEvents.add(event);
			}
		}
		return collectedEvents;
	}

	private ArrayList<Event> sortedByTimestamp(Collection<Event> events) {
		Event[] eventsArray= events.toArray(new Event[] {});
		Arrays.sort(eventsArray, getEventTimestampComparatorForSorting());
		return new ArrayList<Event>(Arrays.asList(eventsArray));
	}

	private Comparator<Event> getEventTimestampComparatorForSorting() {
		return new Comparator<Event>() {

			@Override
			public int compare(Event e1, Event e2) {
				return Long.signum(e1.getTimestamp() - e2.getTimestamp());
			}

		};
	}

	private String normalizedProjectName(String projectName) {
		if (projectName == null || ".workspace".equals(projectName)) {
			return "";
		}
		else {
			return projectName;
		}
	}

	private Comparator<Event> getEventTimestampComparatorForFinding(final long maxTimestampDifference) {
		return new Comparator<Event>() {

			@Override
			public int compare(Event e1, Event e2) {
				if (Math.abs(e1.getTimestamp() - e2.getTimestamp()) < maxTimestampDifference && Utils.toJavaRefactoringID(e1.toMap().get("id")).equals(Utils.toJavaRefactoringID(e2.toMap().get("id")))
						&& normalizedProjectName(e1.toMap().get("project")).equals(normalizedProjectName(e2.toMap().get("project")))) {
					return 0;
				}
				else {
					return Long.signum(e1.getTimestamp() - e2.getTimestamp());
				}
			}

		};
	}

}
