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

	long beforeTimestamp, afterTimestamp;

	Set<DefaultProblemWrapper> afterMinusBefore;

	Set<DefaultProblemWrapper> beforeMinusAfter;

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
		SafeRecorder safeRecorder= new SafeRecorder("refactorings/refactoring-problems.log");
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
		final String XMLWriter_XML_VERSION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		safeRecorder.record(outputStream.toString().substring(XMLWriter_XML_VERSION.length()));
	}
}
