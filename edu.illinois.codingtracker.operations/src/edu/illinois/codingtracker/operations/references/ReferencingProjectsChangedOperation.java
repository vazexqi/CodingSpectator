/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.references;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;

import edu.illinois.codingtracker.jdt.project.manipulation.JavaProjectHelper;
import edu.illinois.codingtracker.operations.JavaProjectsUpkeeper;
import edu.illinois.codingtracker.operations.OperationLexer;
import edu.illinois.codingtracker.operations.OperationSymbols;
import edu.illinois.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingtracker.operations.UserOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
public class ReferencingProjectsChangedOperation extends UserOperation {

	private String projectName;

	private final Set<String> referencingProjectNames= new HashSet<String>();

	public ReferencingProjectsChangedOperation() {
		super();
	}

	public ReferencingProjectsChangedOperation(String projectName, Set<String> referencingProjectNames) {
		super();
		this.projectName= projectName;
		this.referencingProjectNames.addAll(referencingProjectNames);
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.REFERENCING_PROJECTS_CHANGED_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "Changed referencing projects";
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		textChunk.append(projectName);
		textChunk.append(referencingProjectNames.size());
		for (String referencingProjectName : referencingProjectNames) {
			textChunk.append(referencingProjectName);
		}
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		projectName= operationLexer.readString();
		int referencingProjectsCount= operationLexer.readInt();
		for (int i= 0; i < referencingProjectsCount; i++) {
			referencingProjectNames.add(operationLexer.readString());
		}
	}

	@Override
	public void replay() throws Exception {
		IJavaProject javaProject= JavaProjectsUpkeeper.findOrCreateJavaProject(projectName);
		//First, remove all references from other projects to this project
		for (IJavaProject someJavaProject : JavaProjectsUpkeeper.getAllJavaProjects()) {
			JavaProjectHelper.removeFromClasspath(someJavaProject, javaProject.getPath());
		}
		//Next, add this project to projects that are currently referencing it
		for (String referencingProjectName : referencingProjectNames) {
			IJavaProject referencingProject= JavaProjectsUpkeeper.findOrCreateJavaProject(referencingProjectName);
			JavaProjectHelper.addRequiredProject(referencingProject, javaProject);
		}
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("Project name: " + projectName + "\n");
		sb.append("Referencing projects count: " + referencingProjectNames.size() + "\n");
		for (String referencingProjectName : referencingProjectNames) {
			sb.append("Referencing project name: " + referencingProjectName + "\n");
		}
		sb.append(super.toString());
		return sb.toString();
	}

}
