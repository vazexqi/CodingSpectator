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

	private final String postProcessedFileRelativePath;

	private final long startUsageTime;

	private final long stopUsageTime;

	private final long startTimestamp;

	private final long stopTimestamp;

	public UsageTimeInterval(String postprocessedFileRelativePath, long startUsageTime, long stopUsageTime, long startTimestamp, long stopTimestamp) {
		this.postProcessedFileRelativePath= postprocessedFileRelativePath;
		this.startUsageTime= startUsageTime;
		this.stopUsageTime= stopUsageTime;
		this.startTimestamp= startTimestamp;
		this.stopTimestamp= stopTimestamp;
	}

	public String getPostProcessedFileRelativePath() {
		return postProcessedFileRelativePath;
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
		return postProcessedFileRelativePath + separator + startUsageTime + separator + stopUsageTime + separator + startTimestamp + separator + stopTimestamp + "\n";
	}

	public static UsageTimeInterval deserialize(String stringRepresentation) {
		StringTokenizer st= new StringTokenizer(stringRepresentation, separator);
		return new UsageTimeInterval(st.nextToken(), Long.parseLong(st.nextToken()), Long.parseLong(st.nextToken()), Long.parseLong(st.nextToken()), Long.parseLong(st.nextToken()));
	}

}
