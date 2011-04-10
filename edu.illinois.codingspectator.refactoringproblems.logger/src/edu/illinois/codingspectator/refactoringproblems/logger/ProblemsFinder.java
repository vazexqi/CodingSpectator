/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.refactoringproblems.logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
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

	Set<Map<String, DefaultProblemWrapper[]>> problems;

	Set<CompilationUnit> affectedCompilationUnits;

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
			Map<String, CategorizedProblem[]> CUJavaProblems= op.problems;
			problems.add(convertRepresentationOfProblems(CUJavaProblems));
		} finally {
			manager.flushZipFiles(this);
		}
	}

	public Set<Map<String, DefaultProblemWrapper[]>> computeProblems(Set<CompilationUnit> affectedCompilationUnits) throws JavaModelException {
		this.affectedCompilationUnits= affectedCompilationUnits;

		problems= new HashSet<Map<String, DefaultProblemWrapper[]>>();
		for (CompilationUnit compilationUnit : affectedCompilationUnits) {
			computeProblems(compilationUnit);
		}

		return problems;
	}


	private static Map<String, DefaultProblemWrapper[]> convertRepresentationOfProblems(Map<String, CategorizedProblem[]> cUJavaProblems) {
		Map<String, DefaultProblemWrapper[]> convertedRepresentations= new HashMap<String, DefaultProblemWrapper[]>();
		for (String key : cUJavaProblems.keySet()) {
			CategorizedProblem[] defaultProblems= cUJavaProblems.get(key);
			DefaultProblemWrapper[] convertedRepresentation= DefaultProblemWrapper.initializeFromArrays(defaultProblems);
			convertedRepresentations.put(key, convertedRepresentation);
		}
		return convertedRepresentations;
	}
}

/**
 * 
 * Wraps the DefaultProblem type so that we can implement equals and hashCode for performing set
 * operations
 * 
 */
class DefaultProblemWrapper {
	private char[] fileName;

	private String message;

	private int id;

	private String[] arguments;

	private int endPosition;

	private int line;

	private int startPosition;

	private int severity;

	@SuppressWarnings("restriction")
	public DefaultProblemWrapper(CategorizedProblem problem) {
		fileName= problem.getOriginatingFileName();
		message= problem.getMessage();
		id= problem.getID();
		arguments= problem.getArguments();
		endPosition= problem.getSourceEnd();
		line= problem.getSourceLineNumber();
		startPosition= problem.getSourceStart();
		severity= problem.isError() ? ProblemSeverities.Error : ProblemSeverities.Warning;
	}

	public static DefaultProblemWrapper[] initializeFromArrays(CategorizedProblem[] categorizedProblems) {
		DefaultProblemWrapper[] wrappers= new DefaultProblemWrapper[categorizedProblems.length];
		for (int i= 0; i < categorizedProblems.length; i++) {
			wrappers[i]= new DefaultProblemWrapper(categorizedProblems[i]);
		}
		return wrappers;
	}

	/////////////////////////////////
	// Auto-generated through Eclipse
	/////////////////////////////////

	@Override
	public int hashCode() {
		final int prime= 31;
		int result= 1;
		result= prime * result + Arrays.hashCode(arguments);
		result= prime * result + endPosition;
		result= prime * result + Arrays.hashCode(fileName);
		result= prime * result + id;
		result= prime * result + line;
		result= prime * result + ((message == null) ? 0 : message.hashCode());
		result= prime * result + severity;
		result= prime * result + startPosition;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultProblemWrapper other= (DefaultProblemWrapper)obj;
		if (!Arrays.equals(arguments, other.arguments))
			return false;
		if (endPosition != other.endPosition)
			return false;
		if (!Arrays.equals(fileName, other.fileName))
			return false;
		if (id != other.id)
			return false;
		if (line != other.line)
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (severity != other.severity)
			return false;
		if (startPosition != other.startPosition)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DefaultProblemWrapper [fileName=" + Arrays.toString(fileName) + ", message=" + message + ", id=" + id + ", arguments=" + Arrays.toString(arguments) + ", endPosition=" + endPosition
				+ ", line=" + line + ", startPosition=" + startPosition + ", severity=" + severity + "]";
	}


}
