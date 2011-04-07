/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.refactoringproblems.logger;

import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.PerformanceStats;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
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
public class ProblemFinder {

	Set<CompilationUnit> affectedCompilationUnits;

	public ProblemFinder(Set<CompilationUnit> affectedCompilationUnits) {
		this.affectedCompilationUnits= affectedCompilationUnits;
	}

	/**
	 * 
	 * This method is based on org.eclipse.jdt.internal.core.CompilationUnit#reconcile(int, int, WorkingCopyOwner, IProgressMonitor)
	 * 
	 * @param compilationUnit
	 * @throws JavaModelException
	 */
	private void computeProblems(CompilationUnit compilationUnit) throws JavaModelException {
		ReconcileWorkingCopyOperation op= new ReconcileWorkingCopyOperation(compilationUnit, CompilationUnit.NO_AST, ICompilationUnit.FORCE_PROBLEM_DETECTION, DefaultWorkingCopyOwner.PRIMARY);
		JavaModelManager manager= JavaModelManager.getJavaModelManager();
		try {
			manager.cacheZipFiles(this); // cache zip files for performance (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=134172)
			op.runOperation(new NullProgressMonitor());
		} finally {
			manager.flushZipFiles(this);
		}
		System.out.println(op.problems);
	}

	public void computeProblems() throws JavaModelException {
		for (CompilationUnit compilationUnit : affectedCompilationUnits) {
			computeProblems(compilationUnit);
		}
	}

}
