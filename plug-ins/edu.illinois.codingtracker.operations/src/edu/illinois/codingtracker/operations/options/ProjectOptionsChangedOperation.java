/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.options;

import java.util.Map;

import org.eclipse.jdt.core.IJavaProject;

import edu.illinois.codingtracker.operations.JavaProjectsUpkeeper;
import edu.illinois.codingtracker.operations.OperationLexer;
import edu.illinois.codingtracker.operations.OperationSymbols;
import edu.illinois.codingtracker.operations.OperationTextChunk;

/**
 * 
 * @author Stas Negara
 * 
 */
public class ProjectOptionsChangedOperation extends OptionsChangedOperation {

	private String projectName;

	public ProjectOptionsChangedOperation() {
		super();
	}

	public ProjectOptionsChangedOperation(String projectName, Map<String, String> projectOptions) {
		super(projectOptions);
		this.projectName= projectName;
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.PROJECT_OPTIONS_CHANGED_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "Changed project options";
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		textChunk.append(projectName);
		super.populateTextChunk(textChunk);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		projectName= operationLexer.readString();
		super.initializeFrom(operationLexer);
	}

	@Override
	public void replay() throws Exception {
		IJavaProject javaProject= JavaProjectsUpkeeper.findOrCreateJavaProject(projectName);
		javaProject.setOptions(options);
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("Project name: " + projectName + "\n");
		sb.append(super.toString());
		return sb.toString();
	}

}
