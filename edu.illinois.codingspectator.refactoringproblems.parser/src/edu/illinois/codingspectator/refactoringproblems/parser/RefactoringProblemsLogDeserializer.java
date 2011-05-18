/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.refactoringproblems.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import edu.illinois.codingspectator.refactoringproblems.logger.DefaultProblemWrapper;
import edu.illinois.codingspectator.refactoringproblems.logger.ProblemChanges;

/**
 * Deserializes a refactoring-problems log and returns a list of ProblemChanges (@see
 * {@link ProblemChanges}).
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * @author Balaji Ambresh Rajkumar
 * 
 */
@SuppressWarnings("restriction")
public class RefactoringProblemsLogDeserializer extends DefaultHandler {

	Set<DefaultProblemWrapper> problems;

	Set<DefaultProblemWrapper> afterMinusBefore, beforeMinusAfter;

	long afterTimestamp, beforeTimestamp;

	List<ProblemChanges> allProblemChanges;

	private boolean considerTimestamps;

	public RefactoringProblemsLogDeserializer(boolean considerTimestamps) {
		this.considerTimestamps= considerTimestamps;
		this.problems= new HashSet<DefaultProblemWrapper>();
		this.afterMinusBefore= new HashSet<DefaultProblemWrapper>();
		this.beforeMinusAfter= new HashSet<DefaultProblemWrapper>();
		this.afterTimestamp= -1;
		this.beforeTimestamp= -1;
		this.allProblemChanges= new ArrayList<ProblemChanges>();
	}

	public List<ProblemChanges> deserializeRefactoringProblemsLog(String fileName) throws RefactoringProblemsParserException {
		try {
			parseXML(fileName);
		} catch (ParserConfigurationException e) {
			throw new RefactoringProblemsParserException();
		} catch (SAXException e) {
			throw new RefactoringProblemsParserException();
		} catch (FileNotFoundException e) {
			throw new RefactoringProblemsParserException();
		} catch (IOException e) {
			throw new RefactoringProblemsParserException();
		}
		return allProblemChanges;
	}

	private void parseXML(String fileName) throws ParserConfigurationException, SAXException, FileNotFoundException, IOException {
		SAXParser parser;
		parser= createParser();
		parser.parse(new InputSource(wrapFileReaderWithStartEndXMLTag(fileName)), this);
	}

	private Reader wrapFileReaderWithStartEndXMLTag(String fileName) throws FileNotFoundException {
		final String allProblemChangesTagName= "all-problem-changes";
		CompositeReader firstComposite= new CompositeReader(new StringReader("<" + allProblemChangesTagName + ">"), new FileReader(new File(fileName)));
		return new CompositeReader(firstComposite, new StringReader("</" + allProblemChangesTagName + ">"));
	}

	private SAXParser createParser() throws ParserConfigurationException, SAXException {
		final SAXParserFactory factory= SAXParserFactory.newInstance();
		final SAXParser parser= factory.newSAXParser();
		final XMLReader reader= parser.getXMLReader();

		reader.setFeature("http://xml.org/sax/features/validation", false); //$NON-NLS-1$
		reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false); //$NON-NLS-1$
		return parser;
	}


	private int getIntAttribute(Attributes attributes, String attribute) {
		return Integer.valueOf(attributes.getValue(attribute));
	}

	private long getLongAttribute(Attributes attributes, String attribute) {
		return Long.valueOf(attributes.getValue(attribute));
	}

	private String getStringAttribute(Attributes attributes, String attribute) {
		return attributes.getValue(attribute);
	}

	private char[] getCharArrayAttribute(Attributes attributes, String attribute) {
		return attributes.getValue(attribute).toCharArray();
	}

	private String[] getStringArrayAttribute(Attributes attributes, String attribute) {
		String argumentsString= attributes.getValue(attribute);
		String[] arguments= argumentsString.substring("[".length(), argumentsString.length() - ", ]".length()).split(", ");
		return arguments;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equals("problem")) {
			char[] fileName= getCharArrayAttribute(attributes, "fileName");
			String message= getStringAttribute(attributes, "message");
			int id= getIntAttribute(attributes, "id");
			String[] arguments= getStringArrayAttribute(attributes, "arguments");
			int severity= getIntAttribute(attributes, "severity");
			int startPosition= getIntAttribute(attributes, "startPosition");
			int endPosition= getIntAttribute(attributes, "endPosition");
			int line= getIntAttribute(attributes, "line");
			CategorizedProblem problem= new DefaultProblem(fileName, message, id, arguments, severity, startPosition, endPosition, line, -1);
			DefaultProblemWrapper defaultProblemWrapper= new DefaultProblemWrapper(getStringAttribute(attributes, "problemMarker"), problem);
			problems.add(defaultProblemWrapper);
		} else if (qName.equals("after-minus-before")) {
			if (considerTimestamps) {
				afterTimestamp= getLongAttribute(attributes, "timestamp");
			}
			problems.clear();
		} else if (qName.equals("before-minus-after")) {
			if (considerTimestamps) {
				beforeTimestamp= getLongAttribute(attributes, "timestamp");
			}
			problems.clear();
		} else if (qName.equals("problem-changes")) {
			afterMinusBefore.clear();
			beforeMinusAfter.clear();
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equals("after-minus-before")) {
			afterMinusBefore.addAll(problems);
		} else if (qName.equals("before-minus-after")) {
			beforeMinusAfter.addAll(problems);
		} else if (qName.equals("problem-changes")) {
			allProblemChanges.add(new ProblemChanges(afterTimestamp, afterMinusBefore, beforeTimestamp, beforeMinusAfter));
		}
	}
}
