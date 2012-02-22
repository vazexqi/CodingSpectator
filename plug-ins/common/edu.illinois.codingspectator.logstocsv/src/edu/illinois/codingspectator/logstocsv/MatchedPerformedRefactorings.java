/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.logstocsv;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * @author Mohsen Vakilian
 * 
 */
public class MatchedPerformedRefactorings implements Mappable, Comparable<MatchedPerformedRefactorings> {

	private String username;

	private String workspaceID;

	private String codingspectatorVersion;

	private String refactoringID;

	private long codingspectatorTimestamp;

	private long codingtrackerTimestamp;

	public MatchedPerformedRefactorings(String username, String workspaceID, String codingspectatorVersion, String refactoringID, long codingspectatorTimestamp, long codingtrackerTimestamp) {
		this.username= username;
		this.workspaceID= workspaceID;
		this.codingspectatorVersion= codingspectatorVersion;
		this.refactoringID= refactoringID;
		this.codingspectatorTimestamp= codingspectatorTimestamp;
		this.codingtrackerTimestamp= codingtrackerTimestamp;
	}

	public static MatchedPerformedRefactorings createMatchedPerformedRefactorings(RefactoringEvent csEvent, long matchingTimestamp) {
		return new MatchedPerformedRefactorings(csEvent.username, csEvent.workspaceID, csEvent.codingspectatorVersion, Utils.toJavaRefactoringID(csEvent.getRefactoringID()), csEvent.getTimestamp(),
				matchingTimestamp);
	}

	public static MatchedPerformedRefactorings createMatchedPerformedRefactorings(UserOperationEvent ctEvent, long matchingTimestamp) {
		return new MatchedPerformedRefactorings(ctEvent.username, ctEvent.workspaceID, ctEvent.codingspectatorVersion, Utils.toJavaRefactoringID(ctEvent.toMap().get("id")), matchingTimestamp,
				ctEvent.getTimestamp());
	}

	private static boolean isRefactoringUnderStudy(String refactoringID) {
		List<String> refactoringIDsUnderStudy= Arrays.asList(
				"org.eclipse.jdt.ui.promote.temp",
				"org.eclipse.jdt.ui.extract.constant",
				"org.eclipse.jdt.ui.extract.interface",
				"org.eclipse.jdt.ui.extract.temp",
				"org.eclipse.jdt.ui.extract.method",
				"org.eclipse.jdt.ui.extract.superclass",
				"org.eclipse.jdt.ui.inline.constant",
				"org.eclipse.jdt.ui.inline.temp",
				"org.eclipse.jdt.ui.inline.method",
				"org.eclipse.jdt.ui.introduce.factory",
				"org.eclipse.jdt.ui.move",
				"org.eclipse.jdt.ui.move.method",
				"org.eclipse.jdt.ui.move.static",
				"org.eclipse.jdt.ui.pull.up",
				"org.eclipse.jdt.ui.push.down",
				"org.eclipse.jdt.ui.rename.class",
				"org.eclipse.jdt.ui.rename.enum.constant",
				"org.eclipse.jdt.ui.rename.field",
				"org.eclipse.jdt.ui.rename.local.variable",
				"org.eclipse.jdt.ui.rename.method",
				"org.eclipse.jdt.ui.rename.package",
				"org.eclipse.jdt.ui.rename.type.parameter",
				"org.eclipse.jdt.ui.use.supertype"
				);
		return refactoringIDsUnderStudy.contains(refactoringID);
	}

	private String missingDataCollector() {
		if (!isRefactoringUnderStudy(refactoringID)) {
			return "";
		}
		if (codingspectatorTimestamp == -1) {
			return "CodingSpectator";
		} else if (codingtrackerTimestamp == -1) {
			return "CodingTracker";
		} else {
			return "";
		}
	}

	private String toHumanReadableTimestamp(long timestamp) {
		if (timestamp == -1) {
			return "";
		}
		DateFormat dateFormat= SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.FULL, Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("America/Chicago"));
		return dateFormat.format(new Date(timestamp));
	}

	@Override
	public Map<String, String> toMap() {
		Map<String, String> map= new HashMap<String, String>();
		map.put("username", username);
		map.put("workspace ID", workspaceID);
		map.put("codingspectator version", codingspectatorVersion);
		map.put("refactoring ID", refactoringID);
		map.put("codingspectator timestamp", String.valueOf(codingspectatorTimestamp));
		map.put("codingspectator human-readable timestamp", toHumanReadableTimestamp(codingspectatorTimestamp));
		map.put("codingtracker timestamp", String.valueOf(codingtrackerTimestamp));
		map.put("codingtracker human-readable timestamp", toHumanReadableTimestamp(codingtrackerTimestamp));
		map.put("missing", missingDataCollector());
		return map;
	}

	@Override
	public int hashCode() {
		final int prime= 31;
		int result= 1;
		result= prime * result + (int)(codingspectatorTimestamp ^ (codingspectatorTimestamp >>> 32));
		result= prime * result + ((codingspectatorVersion == null) ? 0 : codingspectatorVersion.hashCode());
		result= prime * result + (int)(codingtrackerTimestamp ^ (codingtrackerTimestamp >>> 32));
		result= prime * result + ((refactoringID == null) ? 0 : refactoringID.hashCode());
		result= prime * result + ((username == null) ? 0 : username.hashCode());
		result= prime * result + ((workspaceID == null) ? 0 : workspaceID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MatchedPerformedRefactorings other= (MatchedPerformedRefactorings)obj;
		if (codingspectatorTimestamp != other.codingspectatorTimestamp)
			return false;
		if (codingspectatorVersion == null) {
			if (other.codingspectatorVersion != null)
				return false;
		} else if (!codingspectatorVersion.equals(other.codingspectatorVersion))
			return false;
		if (codingtrackerTimestamp != other.codingtrackerTimestamp)
			return false;
		if (refactoringID == null) {
			if (other.refactoringID != null)
				return false;
		} else if (!refactoringID.equals(other.refactoringID))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		if (workspaceID == null) {
			if (other.workspaceID != null)
				return false;
		} else if (!workspaceID.equals(other.workspaceID))
			return false;
		return true;
	}

	@Override
	public int compareTo(MatchedPerformedRefactorings o) {
		if (username.equals(o.username)) {
			if (workspaceID.equals(o.workspaceID)) {
				if (codingspectatorTimestamp == -1 || o.codingspectatorTimestamp == -1) {
					return Long.signum(codingtrackerTimestamp - o.codingtrackerTimestamp);
				} else {
					return Long.signum(codingspectatorTimestamp - o.codingspectatorTimestamp);
				}
			} else {
				return workspaceID.compareTo(o.workspaceID);
			}
		} else {
			return username.compareTo(o.username);
		}
	}
}
