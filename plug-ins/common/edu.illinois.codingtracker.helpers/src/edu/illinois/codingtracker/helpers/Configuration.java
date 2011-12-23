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

	public static final String postprocessorRootFolderName= System.getenv("POSTPROCESSOR_ROOT_FOLDER");

	public static final boolean isInPostprocessMode= postprocessorRootFolderName != null;

}
