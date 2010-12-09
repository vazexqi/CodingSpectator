/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.refactorings;

import java.util.Map;
import java.util.Set;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

import edu.illinois.codingspectator.codingtracker.helpers.RecorderHelper;
import edu.illinois.codingspectator.codingtracker.operations.UserOperation;
import edu.illinois.codingspectator.codingtracker.recording.TextChunk;

/**
 * 
 * @author Stas Negara
 * 
 * 
 */
public abstract class RefactoringOperation extends UserOperation {

	private final RefactoringDescriptor refactoringDescriptor;

	public RefactoringOperation(RefactoringDescriptor refactoringDescriptor, String operationSymbol, String debugMessage) {
		super(operationSymbol, debugMessage, refactoringDescriptor.getTimeStamp());
		this.refactoringDescriptor= refactoringDescriptor;
	}

	@Override
	@SuppressWarnings("rawtypes")
	protected void populateTextChunk(TextChunk textChunk) {
		textChunk.append(refactoringDescriptor.getID());
		textChunk.append(refactoringDescriptor.getProject());
		textChunk.append(refactoringDescriptor.getFlags());
		Map arguments= RecorderHelper.getRefactoringArguments(refactoringDescriptor);
		if (arguments != null) {
			Set keys= arguments.keySet();
			textChunk.append(keys.size());
			for (Object key : keys) {
				Object value= arguments.get(key);
				textChunk.append(key);
				textChunk.append(value);
			}
		} else {
			textChunk.append(0);
		}
	}

}
