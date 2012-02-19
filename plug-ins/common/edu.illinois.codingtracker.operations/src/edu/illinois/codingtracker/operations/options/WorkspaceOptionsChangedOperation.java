/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.options;

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;

import edu.illinois.codingtracker.operations.OperationSymbols;

/**
 * 
 * @author Stas Negara
 * 
 */
public class WorkspaceOptionsChangedOperation extends OptionsChangedOperation {

	public WorkspaceOptionsChangedOperation() {
		super();
	}

	public WorkspaceOptionsChangedOperation(Map<String, String> workspaceOptions) {
		super(workspaceOptions);
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.WORKSPACE_OPTIONS_CHANGED_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "Changed workspace options";
	}

	@Override
	public void replay() throws Exception {
		JavaCore.setOptions(new Hashtable<String, String>(options));
	}

}
