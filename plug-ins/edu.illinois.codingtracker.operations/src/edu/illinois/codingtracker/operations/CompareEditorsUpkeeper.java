/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.compare.internal.CompareEditor;



/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public class CompareEditorsUpkeeper {

	//TODO: Should be made empty on reset?
	private static final Map<String, CompareEditor> openCompareEditors= new HashMap<String, CompareEditor>();

	public static void addEditor(String editorID, CompareEditor editor) {
		openCompareEditors.put(editorID, editor);
	}

	public static CompareEditor getEditor(String editorID) {
		return openCompareEditors.get(editorID);
	}

	public static void removeEditor(String editorID) {
		openCompareEditors.remove(editorID);
	}

}
