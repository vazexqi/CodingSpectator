/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.options;

import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import edu.illinois.codingspectator.codingtracker.operations.OperationLexer;
import edu.illinois.codingspectator.codingtracker.operations.OperationSymbols;
import edu.illinois.codingspectator.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingtracker.jdt.project.manipulation.JavaProjectHelper;

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
		projectName= operationLexer.getNextLexeme();
		super.initializeFrom(operationLexer);
	}

	@Override
	public void replay() throws Exception {
		IJavaProject javaProject= JavaCore.create(ResourcesPlugin.getWorkspace().getRoot().getProject(projectName));
		if (!javaProject.exists()) {
			javaProject= JavaProjectHelper.createJavaProject(projectName, "bin");
		}
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
