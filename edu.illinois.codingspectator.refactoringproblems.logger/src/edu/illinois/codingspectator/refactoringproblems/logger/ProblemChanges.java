/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.refactoringproblems.logger;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Set;

import org.eclipse.core.internal.resources.XMLWriter;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import edu.illinois.codingspectator.saferecorder.SafeRecorder;

/**
 * This class contains the two way differences between the set of compilation problems before and
 * after a refactoring.
 * 
 * @author Mohsen Vakilian
 * @author Balaji Ambresh Rajkumar
 * 
 */
@SuppressWarnings("restriction")
public class ProblemChanges {

	public static final String REFACTORING_PROBLEMS_LOG= "refactoring-problems.log";

	long beforeTimestamp, afterTimestamp;

	Set<DefaultProblemWrapper> afterMinusBefore;

	Set<DefaultProblemWrapper> beforeMinusAfter;

//	@Override
//	public int hashCode() {
//		final int prime= 31;
//		int result= 1;
//		result= prime * result + ((afterMinusBefore == null) ? 0 : afterMinusBefore.hashCode());
//		result= prime * result + ((beforeMinusAfter == null) ? 0 : beforeMinusAfter.hashCode());
//		return result;
//	}

//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj) {
//			return true;
//		} else if (obj instanceof ProblemChanges == false) {
//			return false;
//		}
//		ProblemChanges rhs= (ProblemChanges)obj;
//		return isEqual(afterMinusBefore, rhs.afterMinusBefore) && isEqual(beforeMinusAfter, rhs.beforeMinusAfter);
//
//	}
	
	
	

//	private boolean isEqual(Set<DefaultProblemWrapper> lhs, Set<DefaultProblemWrapper> rhs) {
//
//		if (lhs.size() != rhs.size()) {
//			return false;
//		}
//		for (DefaultProblemWrapper problemEntry : rhs) {
//			if (lhs.contains(problemEntry) == false) {
//				return false;
//			}
//		}
//		return true;
//	}

	@Override
	public int hashCode() {
		final int prime= 31;
		int result= 1;
		result= prime * result + ((afterMinusBefore == null) ? 0 : afterMinusBefore.hashCode());
		result= prime * result + (int)(afterTimestamp ^ (afterTimestamp >>> 32));
		result= prime * result + ((beforeMinusAfter == null) ? 0 : beforeMinusAfter.hashCode());
		result= prime * result + (int)(beforeTimestamp ^ (beforeTimestamp >>> 32));
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
		ProblemChanges other= (ProblemChanges)obj;
		if (afterMinusBefore == null) {
			if (other.afterMinusBefore != null)
				return false;
		} else if (!afterMinusBefore.equals(other.afterMinusBefore))
			return false;
		if (afterTimestamp != other.afterTimestamp)
			return false;
		if (beforeMinusAfter == null) {
			if (other.beforeMinusAfter != null)
				return false;
		} else if (!beforeMinusAfter.equals(other.beforeMinusAfter))
			return false;
		if (beforeTimestamp != other.beforeTimestamp)
			return false;
		return true;
	}

	public ProblemChanges(long afterTimestamp, Set<DefaultProblemWrapper> afterMinusBefore, long beforeTimestamp, Set<DefaultProblemWrapper> beforeMinusAfter) {
		this.afterTimestamp= afterTimestamp;
		this.afterMinusBefore= afterMinusBefore;
		this.beforeTimestamp= beforeTimestamp;
		this.beforeMinusAfter= beforeMinusAfter;
	}

	private void addTo(Set<DefaultProblemWrapper> problems, String tagName, long timestamp, XMLWriter xmlWriter) throws UnsupportedEncodingException {
		HashMap<String, Object> attributes= new HashMap<String, Object>();
		attributes.put("timestamp", timestamp);
		xmlWriter.startTag(tagName, attributes);
		for (DefaultProblemWrapper problem : problems) {
			problem.addTo(xmlWriter);
		}
		xmlWriter.endTag(tagName);
	}

	@Override
	public String toString() {
		return "ProblemChanges [beforeTimestamp=" + beforeTimestamp + ", afterTimestamp=" + afterTimestamp + ", afterMinusBefore=" + afterMinusBefore + ", beforeMinusAfter=" + beforeMinusAfter + "]";
	}

	/**
	 * 
	 * This method prints out the changes of the problems in the following format:
	 * 
	 * <problem-changes>
	 * 
	 * <after-minus-before timestamp="...">...</after-minus-before>
	 * 
	 * <before-minus-after timestamp="...">...</before- minus-after>
	 * 
	 * </problem-changes>
	 */
	public void log() {
		SafeRecorder safeRecorder= new SafeRecorder("refactorings/" + REFACTORING_PROBLEMS_LOG);
		ByteArrayOutputStream outputStream= new ByteArrayOutputStream();
		try {
			XMLWriter xmlWriter= new XMLWriter(outputStream);
			final String problemChangesTag= "problem-changes";
			xmlWriter.startTag(problemChangesTag, null);
			addTo(afterMinusBefore, "after-minus-before", afterTimestamp, xmlWriter);
			addTo(beforeMinusAfter, "before-minus-after", beforeTimestamp, xmlWriter);
			xmlWriter.endTag(problemChangesTag);
			xmlWriter.close();
		} catch (UnsupportedEncodingException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, "CODINGSPECTATOR: Failed to serialize the compilation problems.", e));
		}
		final String XMLWriter_XML_VERSION= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		safeRecorder.record(outputStream.toString().substring(XMLWriter_XML_VERSION.length()));
	}
}
