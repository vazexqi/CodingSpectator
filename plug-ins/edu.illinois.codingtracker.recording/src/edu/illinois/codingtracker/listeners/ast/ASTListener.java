/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.listeners.ast;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jface.text.DocumentEvent;

import edu.illinois.codingtracker.listeners.BasicListener;

/**
 * 
 * @author Stas Negara
 * 
 */
public class ASTListener extends BasicListener {

	private CoherentTextChange currentTextChange;

	public void beforeDocumentChange(DocumentEvent event) {
		if (currentTextChange == null) {
			currentTextChange= new CoherentTextChange(event.getDocument().get(), event.getOffset(), event.getLength(), event.getText().length());
		} else {
			int newOffset= event.getOffset() - event.getLength();
			if (currentTextChange.shouldGlueNewTextChange(newOffset)) {
				currentTextChange.glueNewTextChange(newOffset, event.getLength(), event.getText().length());
			} else {
				generateAST("BEFORE", currentTextChange.getOldDocumentText(), currentTextChange.getOffset(), currentTextChange.getRemovedTextLength());
				generateAST("AFTER", currentTextChange.getNewDocumentText(), currentTextChange.getOffset(), currentTextChange.getAddedTextLength());
				currentTextChange= new CoherentTextChange(event.getDocument().get(), event.getOffset(), event.getLength(), event.getText().length());
			}
		}
	}

	public void afterDocumentChange(DocumentEvent event) {
		if (currentTextChange == null) {
			throw new RuntimeException("The current coherent text change should not be null after a document was changed: " + event);
		}
		currentTextChange.updateNewDocumentText(event.getDocument().get());
	}

	private void generateAST(String message, String source, int offset, int length) {
		System.out.println(message);
		ASTParser parser= createParser();
		parser.setSource(source.toCharArray());
		ASTNode rootNode= parser.createAST(null);
		//System.out.println("Root node: " + rootNode.toString());
		//System.out.println("Offset=" + offset + ", length=" + length);
		CustomNodeFinder nodeFinder= new CustomNodeFinder(rootNode, offset, length);
		ASTNode coveringNode= nodeFinder.getCoveringNode();
		if (coveringNode == null) {
			//System.out.println("NO COVERING NODE");
		} else {
			//System.out.println("Covering node class: " + coveringNode.getClass());
			//System.out.println("Covering node: " + coveringNode);
		}
		List<ASTNode> coveredNodes= nodeFinder.getCoveredNodes();
		if (coveredNodes.size() == 0) {
			//System.out.println("NO COVERED NODE");
		} else {
			for (ASTNode coveredNode : coveredNodes) {
				//System.out.println("Covered node class: " + coveredNode.getClass());
				//System.out.println("Covered node: " + coveredNode);
			}
		}
	}

	//TODO: Should the parser be created once and then just reused?
	private ASTParser createParser() {
		ASTParser parser= ASTParser.newParser(3);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setIgnoreMethodBodies(false);
		return parser;
	}

}
