/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;

import edu.illinois.codingtracker.jdt.project.manipulation.JavaProjectHelper;


/**
 * 
 * @author Stas Negara
 * 
 */
public class JavaProjectsUpkeeper {

	private static final Set<IJavaProject> javaProjects= new HashSet<IJavaProject>();

	public static IJavaProject[] getAllJavaProjects() {
		return javaProjects.toArray(new IJavaProject[javaProjects.size()]);
	}

	public static IJavaProject findOrCreateJavaProject(String projectName) throws CoreException {
		IJavaProject javaProject= findExistingJavaProject(projectName);
		if (javaProject == null) {
			javaProject= JavaProjectHelper.createJavaProject(projectName, "bin");
			javaProjects.add(javaProject);
		}
		return javaProject;
	}

	private static IJavaProject findExistingJavaProject(String projectName) {
		for (IJavaProject javaProject : javaProjects) {
			if (javaProject.getElementName().equals(projectName)) {
				if (javaProject.exists()) {
					return javaProject;
				} else {
					javaProjects.remove(javaProject);
					return null;
				}

			}
		}
		return null;
	}

}
