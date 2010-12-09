/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.recording;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class Symbols {

	//Used symbols: 23, remaining symbols: v w y

	public static final String CONFLICT_EDITOR_OPENED_SYMBOL= "g";

	public static final String ECLIPSE_SESSION_SYMBOL= "l";

	public static final String FILE_CLOSED_SYMBOL= "c";

	public static final String CONFLICT_EDITOR_CLOSED_SYMBOL= "q";

	public static final String FILE_SAVED_SYMBOL= "s";

	public static final String CONFLICT_EDITOR_SAVED_SYMBOL= "z";

	//Modification outside of Eclipse, or it may be a move/copy refactoring that overwrites a file displayed in a viewer, 
	//or SVN performs Revert operation, or some dirty file is changed externally and then saved (without refreshing)
	public static final String FILE_EXTERNALLY_MODIFIED_SYMBOL= "x";

	public static final String FILE_UPDATED_SYMBOL= "m";

	public static final String FILE_INITIALLY_COMMITTED_SYMBOL= "i";

	public static final String FILE_COMMITTED_SYMBOL= "o";

	public static final String FILE_REFACTORED_SAVED_SYMBOL= "a";

	public static final String FILE_NEW_SYMBOL= "f";

	public static final String FILE_EDITED_SYMBOL= "e";

	public static final String TEXT_CHANGE_PERFORMED_SYMBOL= "t";

	public static final String TEXT_CHANGE_UNDONE_SYMBOL= "h";

	public static final String TEXT_CHANGE_REDONE_SYMBOL= "d";

	public static final String CONFLICT_EDITOR_TEXT_CHANGE_PERFORMED_SYMBOL= "j";

	public static final String CONFLICT_EDITOR_TEXT_CHANGE_UNDONE_SYMBOL= "k";

	public static final String CONFLICT_EDITOR_TEXT_CHANGE_REDONE_SYMBOL= "n";

	public static final String REFACTORING_STARTED_SYMBOL= "b";

	public static final String REFACTORING_PERFORMED_SYMBOL= "p";

	public static final String REFACTORING_UNDONE_SYMBOL= "u";

	public static final String REFACTORING_REDONE_SYMBOL= "r";

}
