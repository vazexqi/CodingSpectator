/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.refactoringproblems.logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Deserializes a refactoring-problems log and returns a list of ProblemChanges (@see
 * {@link ProblemChanges}).
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class RefactoringProblemsLogDeserializer extends DefaultHandler {

	public List<ProblemChanges> deserializeRefactoringProblemsLog(String fileName) {
		parseXML(fileName);
		return null;
	}

	private void parseXML(String fileName) {
		try {
			SAXParser parser= createParser();
			parser.parse(new InputSource(wrapFileReaderWithStartEndXMLTag(fileName)), this);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Reader wrapFileReaderWithStartEndXMLTag(String fileName) throws FileNotFoundException {
		final String fauxRootTagName= "root";
		CompositeReader firstComposite= new CompositeReader(new StringReader("<" + fauxRootTagName + ">"), new FileReader(new File(fileName)));
		return new CompositeReader(firstComposite, new StringReader("</" + fauxRootTagName + ">"));
	}

	private SAXParser createParser() throws ParserConfigurationException, SAXException {

		final SAXParserFactory factory= SAXParserFactory.newInstance();
		final SAXParser parser= factory.newSAXParser();
		final XMLReader reader= parser.getXMLReader();

		try {

			reader.setFeature("http://xml.org/sax/features/validation", false); //$NON-NLS-1$
			reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false); //$NON-NLS-1$

		} catch (SAXNotRecognizedException exception) {
			// Do nothing
		} catch (SAXNotSupportedException exception) {
			// Do nothing
		}
		return parser;
	}

	//TODO: Implement the methods in DefaultHandler to create the deserialize and create the appropriate objects.
	// e.g. startElement(...)

}
