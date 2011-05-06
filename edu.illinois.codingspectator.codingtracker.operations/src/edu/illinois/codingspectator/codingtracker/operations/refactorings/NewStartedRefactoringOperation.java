/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.refactorings;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.internal.core.refactoring.history.DefaultRefactoringDescriptor;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringContributionManager;

import edu.illinois.codingspectator.codingtracker.operations.OperationLexer;
import edu.illinois.codingspectator.codingtracker.operations.OperationSymbols;
import edu.illinois.codingspectator.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingspectator.codingtracker.operations.UserOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings({ "rawtypes", "restriction" })
public class NewStartedRefactoringOperation extends UserOperation {

	public static enum Mode {
		PERFORM, UNDO, REDO
	};

	private Mode mode;

	private String id;

	private String project;

	private int flags;

	//TreeMap is required for the deterministic behavior that is expected by the tests
	private final Map<String, String> arguments= new TreeMap<String, String>();


	public NewStartedRefactoringOperation() {
		super();
	}

	public NewStartedRefactoringOperation(Mode mode, RefactoringDescriptor refactoringDescriptor) {
		super(refactoringDescriptor.getTimeStamp());
		this.mode= mode;
		id= refactoringDescriptor.getID();
		project= refactoringDescriptor.getProject();
		flags= refactoringDescriptor.getFlags();
		initializeArguments(getRefactoringArguments(refactoringDescriptor));
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.NEW_REFACTORING_STARTED_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "[new] Started refactoring";
	}

	private Map getRefactoringArguments(RefactoringDescriptor refactoringDescriptor) {
		Map arguments= null;
		RefactoringContribution refactoringContribution=
				RefactoringContributionManager.getInstance().getRefactoringContribution(refactoringDescriptor.getID());
		if (refactoringContribution != null)
			arguments= refactoringContribution.retrieveArgumentMap(refactoringDescriptor);
		else if (refactoringDescriptor instanceof DefaultRefactoringDescriptor)
			arguments= ((DefaultRefactoringDescriptor)refactoringDescriptor).getArguments();
		return arguments;
	}

	private void initializeArguments(Map refactoringArguments) {
		if (refactoringArguments != null) {
			for (Object key : refactoringArguments.keySet()) {
				Object value= refactoringArguments.get(key);
				arguments.put(key.toString(), value.toString());
			}
		}
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		textChunk.append(mode.ordinal());
		textChunk.append(id);
		textChunk.append(project);
		textChunk.append(flags);
		textChunk.append(arguments.size());
		for (Entry<String, String> argumentEntry : arguments.entrySet()) {
			textChunk.append(argumentEntry.getKey());
			textChunk.append(argumentEntry.getValue());
		}
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		mode= Mode.values()[operationLexer.readInt()];
		id= operationLexer.readString();
		project= operationLexer.readString();
		flags= operationLexer.readInt();
		int argumentsCount= operationLexer.readInt();
		for (int i= 0; i < argumentsCount; i++) {
			arguments.put(operationLexer.readString(), operationLexer.readString());
		}
	}

	@Override
	public void replay() {
		//do nothing
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("Mode: " + mode + "\n");
		sb.append("ID: " + id + "\n");
		sb.append("Project: " + project + "\n");
		sb.append("Flags: " + flags + "\n");
		sb.append("Arguments count: " + arguments.size() + "\n");
		for (Entry<String, String> argumentEntry : arguments.entrySet()) {
			sb.append("Key: " + argumentEntry.getKey() + "\n");
			sb.append("Value: " + argumentEntry.getValue() + "\n");
		}
		sb.append(super.toString());
		return sb.toString();
	}

}
