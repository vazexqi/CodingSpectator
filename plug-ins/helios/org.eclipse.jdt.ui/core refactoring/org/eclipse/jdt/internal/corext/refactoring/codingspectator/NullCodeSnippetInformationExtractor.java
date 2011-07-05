/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package org.eclipse.jdt.internal.corext.refactoring.codingspectator;

import org.eclipse.ltk.core.refactoring.codingspectator.CodeSnippetInformation;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class NullCodeSnippetInformationExtractor extends CodeSnippetInformationExtractor {

	public CodeSnippetInformation extractCodeSnippetInformation() {
		return new CodeSnippetInformation();
	}

	protected ASTNode findTargetNode() {
		throw new UnsupportedOperationException();
	}

}
