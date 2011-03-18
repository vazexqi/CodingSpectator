/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class OperationSymbols {

	//Used symbols: 26 + 7, remaining symbols: F G H J K L M N P Q R S T U V W X Y Z

	public static final char ECLIPSE_STARTED_SYMBOL= 'l';

	public static final char REFACTORING_STARTED_SYMBOL= 'b';

	public static final char REFACTORING_PERFORMED_SYMBOL= 'p';

	public static final char REFACTORING_UNDONE_SYMBOL= 'u';

	public static final char REFACTORING_REDONE_SYMBOL= 'r';

	public static final char CONFLICT_EDITOR_OPENED_SYMBOL= 'g';

	public static final char CONFLICT_EDITOR_CLOSED_SYMBOL= 'q';

	public static final char CONFLICT_EDITOR_SAVED_SYMBOL= 'z';

	public static final char FILE_CLOSED_SYMBOL= 'c';

	public static final char FILE_SAVED_SYMBOL= 's';

	//Modification outside of Eclipse, or it may be a move/copy refactoring that overwrites a file displayed in a viewer, 
	//or SVN performs Revert operation, or some dirty file is changed externally and then saved (without refreshing)
	public static final char FILE_EXTERNALLY_MODIFIED_SYMBOL= 'x';

	public static final char FILE_UPDATED_SYMBOL= 'm';

	public static final char FILE_SVN_INITIALLY_COMMITTED_SYMBOL= 'i';

	public static final char FILE_CVS_INITIALLY_COMMITTED_SYMBOL= 'I';

	public static final char FILE_SVN_COMMITTED_SYMBOL= 'o';

	public static final char FILE_CVS_COMMITTED_SYMBOL= 'O';

	public static final char FILE_REFACTORED_SAVED_SYMBOL= 'a';

	public static final char FILE_NEW_SYMBOL= 'f';

	public static final char FILE_EDITED_SYMBOL= 'e';

	public static final char TEXT_CHANGE_PERFORMED_SYMBOL= 't';

	public static final char TEXT_CHANGE_UNDONE_SYMBOL= 'h';

	public static final char TEXT_CHANGE_REDONE_SYMBOL= 'd';

	public static final char CONFLICT_EDITOR_TEXT_CHANGE_PERFORMED_SYMBOL= 'j';

	public static final char CONFLICT_EDITOR_TEXT_CHANGE_UNDONE_SYMBOL= 'k';

	public static final char CONFLICT_EDITOR_TEXT_CHANGE_REDONE_SYMBOL= 'n';

	public static final char TEST_SESSION_LAUNCHED_SYMBOL= 'v';

	public static final char TEST_SESSION_STARTED_SYMBOL= 'w';

	public static final char TEST_SESSION_FINISHED_SYMBOL= 'y';

	public static final char TEST_CASE_STARTED_SYMBOL= 'A';

	public static final char TEST_CASE_FINISHED_SYMBOL= 'B';

	public static final char APPLICATION_LAUNCHED_SYMBOL= 'C';

	public static final char WORKSPACE_OPTIONS_CHANGED_SYMBOL= 'D';

	public static final char PROJECT_OPTIONS_CHANGED_SYMBOL= 'E';

}
