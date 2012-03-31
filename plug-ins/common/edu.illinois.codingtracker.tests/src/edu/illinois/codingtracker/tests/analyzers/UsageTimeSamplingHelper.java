/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import edu.illinois.codingtracker.helpers.Configuration;
import edu.illinois.codingtracker.helpers.ResourceHelper;



/**
 * 
 * @author Stas Negara
 * 
 */
public class UsageTimeSamplingHelper {

	private static final long sampleInterval= 30 * 60 * 1000; // 30 minutes

	private static final List<UsageTimeInterval> usageTimeIntervals= new LinkedList<UsageTimeInterval>();


	public static void main(String[] args) {
		File usageTimeFile= new File(Configuration.postprocessorRootFolderName, "combined.usage_time.aux");
		if (!usageTimeFile.exists()) {
			throw new RuntimeException("Could not find the usage time auxiliary file.");
		}
		String usageTimeFileContent= ResourceHelper.readFileContent(usageTimeFile);
		StringTokenizer st= new StringTokenizer(usageTimeFileContent, "\n");
		while (st.hasMoreTokens()) {
			usageTimeIntervals.add(UsageTimeInterval.deserialize(st.nextToken()));
		}
		interact();
	}

	private static void interact() {
		BufferedReader bufferedReader= new BufferedReader(new InputStreamReader(System.in));
		String line;
		while (true) {
			System.out.println("Input a time point or type 'q' to quit:");
			try {
				line= bufferedReader.readLine();
			} catch (IOException e) {
				throw new RuntimeException("Could not read the line");
			}
			if (line.equals("q")) {
				System.out.println("bye-bye");
				return;
			}
			boolean foundInterval= handleTimePoint(Long.parseLong(line));
			if (!foundInterval) {
				System.out.println("Could not find an interval for the specified time point.");
			}
		}
	}

	private static boolean handleTimePoint(long timePoint) {
		for (UsageTimeInterval interval : usageTimeIntervals) {
			if (interval.getStartUsageTime() <= timePoint && interval.getStopUsageTime() > timePoint) {
				if (interval.getStopUsageTime() >= timePoint + sampleInterval) {
					System.out.println("The desired sequence is located at: " + interval.getPostProcessedFileRelativePath());
					System.out.println("The desired timestamp is: " + (interval.getStartTimestamp() + timePoint - interval.getStartUsageTime()));
				} else {
					System.out.println("Found an interval, but there is not enough to replay for a sample interval.");
				}
				return true;
			}
		}
		return false;
	}

}
