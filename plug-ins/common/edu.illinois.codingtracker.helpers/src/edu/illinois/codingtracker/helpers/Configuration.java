/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.helpers;


/**
 * 
 * @author Stas Negara
 * 
 */
public class Configuration {

	public static final boolean isInDebugMode= System.getenv("DEBUG_MODE") != null;

	public static final boolean isInTestMode= System.getenv("TEST_MODE") != null;

	public static final boolean isOldFormat= System.getenv("OLD_CODINGTRACKER_FORMAT") != null;

	public static final boolean isOldASTFormat= System.getenv("OLD_AST_CODINGTRACKER_FORMAT") != null;

	public static final String postprocessorRootFolderName= System.getenv("POSTPROCESSOR_ROOT_FOLDER");

	public static final long usageTimeStart;

	public static final long usageTimeStop;

	public static final boolean isInPostprocessMode= postprocessorRootFolderName != null;

	public static final boolean isInASTInferenceMode= System.getenv("AST_INFERENCE_MODE") != null;

	public static final boolean isInRefactoringInferenceMode= System.getenv("REFACTORING_INFERENCE_MODE") != null;

	public static final boolean isInReplayMode= System.getenv("REPLAY_MODE") != null;

	public static final int miningFrequencyThreshold;

	public static final long miningMaxBlockSize;


	static {
		String envUsageTimeStart= System.getenv("USAGE_TIME_START");
		if (envUsageTimeStart != null) {
			usageTimeStart= Long.parseLong(envUsageTimeStart);
		} else {
			usageTimeStart= Long.MIN_VALUE;
		}

		String envUsageTimeStop= System.getenv("USAGE_TIME_STOP");
		if (envUsageTimeStop != null) {
			usageTimeStop= Long.parseLong(envUsageTimeStop);
		} else {
			usageTimeStop= Long.MAX_VALUE;
		}

		String envFrequencyThreshold= System.getenv("MINING_FREQUENCY_THRESHOLD");
		if (envFrequencyThreshold != null) {
			miningFrequencyThreshold= Integer.parseInt(envFrequencyThreshold);
		} else {
			miningFrequencyThreshold= 1;
		}

		String envMiningMaxBlockSize= System.getenv("MINING_MAX_BLOCK_SIZE"); //Is defined in minutes.
		if (envMiningMaxBlockSize != null) {
			miningMaxBlockSize= Integer.parseInt(envMiningMaxBlockSize) * 60 * 1000;
		} else {
			miningMaxBlockSize= 5 * 60 * 1000; //5 minutes in milliseconds.
		}
	}

}
