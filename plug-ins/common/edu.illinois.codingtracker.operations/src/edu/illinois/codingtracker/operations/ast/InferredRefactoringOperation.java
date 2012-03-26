/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.ast;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import edu.illinois.codingtracker.operations.OperationLexer;
import edu.illinois.codingtracker.operations.OperationSymbols;
import edu.illinois.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingtracker.operations.UserOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
public class InferredRefactoringOperation extends UserOperation {

	public static enum RefactoringKind {
		EXTRACT_LOCAL_VARIABLE, INLINE_LOCAL_VARIABLE, RENAME_LOCAL_VARIABLE, RENAME_FIELD, RENAME_METHOD, RENAME_CLASS, PROMOTE_TEMP, EXTRACT_CONSTANT, EXTRACT_METHOD, ENCAPSULATE_FIELD
	};

	private RefactoringKind refactoringKind;

	private long refactoringID;

	private final Map<String, String> arguments= new TreeMap<String, String>(); //TreeMap ensures a particular order.


	public static boolean isExtract(RefactoringKind refactoringKind) {
		return refactoringKind == RefactoringKind.EXTRACT_LOCAL_VARIABLE ||
				refactoringKind == RefactoringKind.EXTRACT_CONSTANT || refactoringKind == RefactoringKind.EXTRACT_METHOD;
	}

	public static boolean isRename(RefactoringKind refactoringKind) {
		return refactoringKind == RefactoringKind.RENAME_LOCAL_VARIABLE || refactoringKind == RefactoringKind.RENAME_FIELD ||
				refactoringKind == RefactoringKind.RENAME_METHOD || refactoringKind == RefactoringKind.RENAME_CLASS;
	}

	public InferredRefactoringOperation() {
		super();
	}

	public InferredRefactoringOperation(RefactoringKind refactoringKind, long refactoringID, Map<String, String> arguments, long timestamp) {
		super(timestamp);
		this.refactoringKind= refactoringKind;
		this.refactoringID= refactoringID;
		for (Entry<String, String> argumentEntry : arguments.entrySet()) {
			this.arguments.put(argumentEntry.getKey(), argumentEntry.getValue());
		}
	}


	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.INFERRED_REFACTORING_OPERATION_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "Inferred refactoring";
	}

	public RefactoringKind getRefactoringKind() {
		return refactoringKind;
	}

	public long getRefactoringID() {
		return refactoringID;
	}

	public Map<String, String> getArguments() {
		return arguments; //TODO: Returning a reference to a collection is not safe.
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		textChunk.append(refactoringKind.ordinal());
		textChunk.append(refactoringID);
		textChunk.append(arguments.size());
		for (Entry<String, String> argumentEntry : arguments.entrySet()) {
			textChunk.append(argumentEntry.getKey());
			textChunk.append(argumentEntry.getValue());
		}
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		refactoringKind= RefactoringKind.values()[operationLexer.readInt()];
		refactoringID= operationLexer.readLong();
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
		sb.append("Refactoring kind: " + refactoringKind + "\n");
		sb.append("Refactoring ID: " + refactoringID + "\n");
		sb.append("Arguments count: " + arguments.size() + "\n");
		for (Entry<String, String> argumentEntry : arguments.entrySet()) {
			sb.append("Key: " + argumentEntry.getKey() + "\n");
			sb.append("Value: " + argumentEntry.getValue() + "\n");
		}
		sb.append(super.toString());
		return sb.toString();
	}

}
