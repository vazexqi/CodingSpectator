/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers;

import java.util.StringTokenizer;



/**
 * 
 * @author Stas Negara
 * 
 */
public class UsageTimeInterval {

	private static final String separator= ":";

	private final String username;

	private final String workspaceID;

	private final String version;

	private final long startUsageTime;

	private final long stopUsageTime;

	private final long startTimestamp;

	private final long stopTimestamp;

	public UsageTimeInterval(String username, String workspaceID, String version, long startUsageTime, long stopUsageTime, long startTimestamp, long stopTimestamp) {
		this.username= username;
		this.workspaceID= workspaceID;
		this.version= version;
		this.startUsageTime= startUsageTime;
		this.stopUsageTime= stopUsageTime;
		this.startTimestamp= startTimestamp;
		this.stopTimestamp= stopTimestamp;
	}

	public String getUsername() {
		return username;
	}

	public String getWorkspaceID() {
		return workspaceID;
	}

	public String getVersion() {
		return version;
	}

	public long getStartUsageTime() {
		return startUsageTime;
	}

	public long getStopUsageTime() {
		return stopUsageTime;
	}

	public long getStartTimestamp() {
		return startTimestamp;
	}

	public long getStopTimestamp() {
		return stopTimestamp;
	}

	public String serialize() {
		return username + separator + workspaceID + separator + version + separator + startUsageTime + separator + stopUsageTime + separator + startTimestamp + separator + stopTimestamp + "\n";
	}

	public static UsageTimeInterval deserialize(String stringRepresentation) {
		StringTokenizer st= new StringTokenizer(stringRepresentation, separator);
		return new UsageTimeInterval(st.nextToken(), st.nextToken(), st.nextToken(), Long.parseLong(st.nextToken()), Long.parseLong(st.nextToken()), Long.parseLong(st.nextToken()),
				Long.parseLong(st.nextToken()));
	}

}
