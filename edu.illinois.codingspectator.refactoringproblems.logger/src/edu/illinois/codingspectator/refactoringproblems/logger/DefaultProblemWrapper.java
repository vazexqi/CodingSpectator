package edu.illinois.codingspectator.refactoringproblems.logger;

/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.internal.resources.XMLWriter;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

/**
 * 
 * Wraps the DefaultProblem type so that we can implement equals and hashCode for performing set
 * operations
 * 
 * @author Balaji Ambresh Rajkumar
 * @author Mohsen Vakilian
 * @author nchen
 * @author Stas Negara
 * 
 */

@SuppressWarnings("restriction")
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

	private static String PROBLEM_TAG= "problem";

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

		xmlWriter.startTag(PROBLEM_TAG, parameters);
		xmlWriter.endTag(PROBLEM_TAG);
		xmlWriter.flush();
	}
}
