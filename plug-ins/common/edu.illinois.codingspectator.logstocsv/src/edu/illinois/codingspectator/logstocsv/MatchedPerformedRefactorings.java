/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.logstocsv;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mohsen Vakilian
 * 
 */
public class MatchedPerformedRefactorings implements Mappable {

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

	@Override
	public Map<String, String> toMap() {
		Map<String, String> map= new HashMap<String, String>();
		map.put("username", username);
		map.put("workspace ID", workspaceID);
		map.put("codingspectator version", codingspectatorVersion);
		map.put("refactoring ID", refactoringID);
		map.put("codingspectator timestamp", String.valueOf(codingspectatorTimestamp));
		map.put("codingtracker timestamp", String.valueOf(codingtrackerTimestamp));
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

}
