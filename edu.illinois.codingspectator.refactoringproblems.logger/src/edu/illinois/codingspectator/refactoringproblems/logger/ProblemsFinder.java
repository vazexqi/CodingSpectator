/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.refactoringproblems.logger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.ReconcileWorkingCopyOperation;

/**
 * 
 * @author Balaji Ambresh Rajkumar
 * @author Mohsen Vakilian
 * @author nchen
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public class ProblemsFinder {

	Set<DefaultProblemWrapper> problems;

	Set<CompilationUnit> affectedCompilationUnits;

	private final static Set<String> problemMarkersToReport;

	static {
		problemMarkersToReport= new HashSet<String>();
		problemMarkersToReport.add(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER);
	}

	/**
	 * 
	 * This method is based on org.eclipse.jdt.internal.core.CompilationUnit#reconcile(int, int,
	 * WorkingCopyOwner, IProgressMonitor)
	 * 
	 * @param compilationUnit
	 * @throws JavaModelException
	 */
	@SuppressWarnings("unchecked")
	private void computeProblems(CompilationUnit compilationUnit) throws JavaModelException {
		ReconcileWorkingCopyOperation op= new ReconcileWorkingCopyOperation(compilationUnit, CompilationUnit.NO_AST, ICompilationUnit.FORCE_PROBLEM_DETECTION, DefaultWorkingCopyOwner.PRIMARY);
		JavaModelManager manager= JavaModelManager.getJavaModelManager();
		try {
			manager.cacheZipFiles(this); // cache zip files for performance (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=134172)
			op.runOperation(new NullProgressMonitor());
			Map<String, CategorizedProblem[]> cuJavaProblems= op.problems;
			problems.addAll(convertRepresentationOfProblems(cuJavaProblems));
		} finally {
			manager.flushZipFiles(this);
		}
	}

	public Set<DefaultProblemWrapper> computeProblems(Set<CompilationUnit> affectedCompilationUnits) throws JavaModelException {
		this.affectedCompilationUnits= affectedCompilationUnits;

		problems= new HashSet<DefaultProblemWrapper>();
		for (CompilationUnit compilationUnit : affectedCompilationUnits) {
			if (compilationUnit.exists()) {
				computeProblems(compilationUnit);
			}
		}

		return problems;
	}


	private static Set<DefaultProblemWrapper> convertRepresentationOfProblems(Map<String, CategorizedProblem[]> cuJavaProblems) {
		Set<DefaultProblemWrapper> convertedRepresentations= new HashSet<DefaultProblemWrapper>();
		for (String problemMarker : cuJavaProblems.keySet()) {
			if (problemMarkersToReport.contains(problemMarker)) {
				CategorizedProblem[] defaultProblems= cuJavaProblems.get(problemMarker);
				Set<DefaultProblemWrapper> convertedRepresentation= DefaultProblemWrapper.initializeFromArrays(problemMarker, defaultProblems);
				convertedRepresentations.addAll(convertedRepresentation);
			}
		}
		return convertedRepresentations;
	}
}
