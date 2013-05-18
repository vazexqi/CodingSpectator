package edu.illinois.codingtracker.codeskimmer;

/**
 * A class for keeping constant values used throughout CodeSkimmer. More things
 * could certainly be moved here...
 * 
 * @author Connor Simmons
 * 
 */
public class CodeSkimmerConstants {

	public static final long MAX_REPLAY_DELAY_TIME = 3000; // in milliseconds

	/*
	 * VIEW OPTIONS
	 */
	public static final int WIDGET_HEIGHT = 200; // in pixels
	public static final int TAB_PANE_WIDTH = 300; // in pixels

	public static enum VisualizationType {
		SEQUENCE, INTENSITY;
	}

	public static final String[] VISUALIZATION_OPTION_NAMES = {
			"Sequence View", "Intensity View" };

	/*
	 * Sequence View Constants
	 */
	public static final int SEQUENCE_VIEW_KEY_WIDTH = 100; // in pixels

	/*
	 * Intensity View Constants
	 */
	public static final int DEFAULT_TIME_WINDOW = 3000; // 3 seconds in
														// milliseconds
	public static final int MIN_GRAPH_WIDTH = 600;

}
