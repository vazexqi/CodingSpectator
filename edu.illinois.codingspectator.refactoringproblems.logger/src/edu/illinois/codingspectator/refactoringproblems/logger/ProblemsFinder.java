/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.refactoringproblems.logger;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.internal.resources.XMLWriter;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModelMarker;
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
			computeProblems(compilationUnit);
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

/**
 * 
 * Wraps the DefaultProblem type so that we can implement equals and hashCode for performing set
 * operations
 * 
 */
class DefaultProblemWrapper {

	private String problemMarker;

	private String fileName;

	private String message;

	private int id;

	private String[] arguments;

	private int endPosition;

	private int line;

	private int startPosition;

	private int severity;

	@SuppressWarnings("restriction")
	public DefaultProblemWrapper(String problemMarker, CategorizedProblem problem) {
		this.problemMarker= problemMarker;
		fileName= new String(problem.getOriginatingFileName());
		message= problem.getMessage();
		id= problem.getID();
		arguments= problem.getArguments();
		endPosition= problem.getSourceEnd();
		line= problem.getSourceLineNumber();
		startPosition= problem.getSourceStart();
		severity= problem.isError() ? ProblemSeverities.Error : ProblemSeverities.Warning;
	}

	public static Set<DefaultProblemWrapper> initializeFromArrays(String problemMarker, CategorizedProblem[] categorizedProblems) {
		Set<DefaultProblemWrapper> wrappers= new HashSet<DefaultProblemWrapper>(categorizedProblems.length);
		for (CategorizedProblem categorizedProblem : categorizedProblems) {
			wrappers.add(new DefaultProblemWrapper(problemMarker, categorizedProblem));
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
		result= prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		result= prime * result + id;
		result= prime * result + line;
		result= prime * result + ((message == null) ? 0 : message.hashCode());
		result= prime * result + ((problemMarker == null) ? 0 : problemMarker.hashCode());
		result= prime * result + severity;
		result= prime * result + startPosition;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		DefaultProblemWrapper other= (DefaultProblemWrapper)obj;
		if (!Arrays.equals(arguments, other.arguments)) {
			return false;
		}
		if (endPosition != other.endPosition) {
			return false;
		}
		if (fileName == null) {
			if (other.fileName != null) {
				return false;
			}
		} else if (!fileName.equals(other.fileName)) {
			return false;
		}
		if (id != other.id) {
			return false;
		}
		if (line != other.line) {
			return false;
		}
		if (message == null) {
			if (other.message != null) {
				return false;
			}
		} else if (!message.equals(other.message)) {
			return false;
		}
		if (problemMarker == null) {
			if (other.problemMarker != null) {
				return false;
			}
		} else if (!problemMarker.equals(other.problemMarker)) {
			return false;
		}
		if (severity != other.severity) {
			return false;
		}
		if (startPosition != other.startPosition) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "DefaultProblemWrapper [problemMarker=" + problemMarker + ", fileName=" + fileName + ", message=" + message + ", id=" + id + ", arguments=" + Arrays.toString(arguments)
				+ ", endPosition=" + endPosition + ", line=" + line + ", startPosition=" + startPosition + ", severity=" + severity + "]";
	}

	@SuppressWarnings("restriction")
	public void addTo(XMLWriter xmlWriter) throws UnsupportedEncodingException {
		HashMap<String, Object> parameters= new HashMap<String, Object>();
		parameters.put("problemMarker", problemMarker);
		parameters.put("fileName", fileName);
		parameters.put("message", message);
		parameters.put("id", id);
		parameters.put("arguments", Arrays.toString(arguments));
		parameters.put("endPosition", endPosition);
		parameters.put("line", line);
		parameters.put("startPosition", startPosition);
		parameters.put("severity", severity);

		xmlWriter.printTag("problem", parameters);
		xmlWriter.flush();
	}
}
