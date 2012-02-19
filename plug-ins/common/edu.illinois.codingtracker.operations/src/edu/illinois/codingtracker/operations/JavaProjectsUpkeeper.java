/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import edu.illinois.codingtracker.compare.helpers.EditorHelper;
import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.jdt.project.manipulation.JavaProjectHelper;


/**
 * 
 * @author Stas Negara
 * 
 */
public class JavaProjectsUpkeeper {

	public static IJavaProject[] getAllJavaProjects() {
		IProject[] projects= getAllProjects();
		IJavaProject[] javaProjects= new IJavaProject[projects.length]; //assumes that during replay all projects are Java projects
		for (int i= 0; i < projects.length; i++) {
			javaProjects[i]= JavaCore.create(projects[i]);
		}
		return javaProjects;
	}

	public static IJavaProject findOrCreateJavaProject(String projectName) throws CoreException {
		IProject project= ResourceHelper.getWorkspaceRoot().getProject(projectName);
		if (project.exists()) {
			return JavaCore.create(project);
		} else {
			return JavaProjectHelper.createJavaProject(projectName, "_some_weird_name_");
		}
	}

	public static void clearWorkspace() {
		EditorHelper.closeAllEditors();
		for (IProject project : getAllProjects()) {
			try {
				JavaProjectHelper.delete(project);
			} catch (CoreException e) {
				throw new RuntimeException("Could not delete project \"" + project.getName() + "\"", e);
			}
		}
	}

	private static IProject[] getAllProjects() {
		return ResourceHelper.getWorkspaceRoot().getProjects();
	}

}
