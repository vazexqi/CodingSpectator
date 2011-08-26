/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.refactorings;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.IUndoManager;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.internal.core.refactoring.history.DefaultRefactoringDescriptor;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringContributionManager;

import edu.illinois.codingtracker.helpers.Debugger;
import edu.illinois.codingtracker.operations.OperationLexer;
import edu.illinois.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingtracker.operations.UserOperation;

/**
 * Concrete implementations of this operation are no longer recorded.
 * 
 * {@see NewStartedRefactoringOperation, FinishedRefactoringOperation}.
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings({ "rawtypes", "restriction" })
public abstract class RefactoringOperation extends UserOperation {

	protected static Set<Long> unperformedRefactorings= new HashSet<Long>();

	private String id;

	private String project;

	private int flags;

	//TreeMap is required for the deterministic behavior that is expected by the tests
	private final Map<String, String> arguments= new TreeMap<String, String>();

	public RefactoringOperation() {
		super();
	}

	public RefactoringOperation(RefactoringDescriptor refactoringDescriptor) {
		super(refactoringDescriptor.getTimeStamp());
		id= refactoringDescriptor.getID();
		project= refactoringDescriptor.getProject();
		flags= refactoringDescriptor.getFlags();
		initializeArguments(getRefactoringArguments(refactoringDescriptor));
	}

	public String getID() {
		return id;
	}

	public String getProject() {
		return project;
	}

	public int getFlags() {
		return flags;
	}

	/**
	 * Note that returning a reference to a private collection breaks incapsulation. But considering
	 * that this is a deprecated class, and this method is used only for its update to the replacing
	 * class, it should be OK.
	 * 
	 * @return
	 */
	public Map<String, String> getArguments() {
		return arguments;
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

	protected IUndoManager getRefactoringUndoManager() {
		return RefactoringCore.getUndoManager();
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
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
		id= operationLexer.readString();
		project= operationLexer.readString();
		flags= operationLexer.readInt();
		int argumentsCount= operationLexer.readInt();
		for (int i= 0; i < argumentsCount; i++) {
			arguments.put(operationLexer.readString(), operationLexer.readString());
		}
	}

	@Override
	public void replay() throws CoreException {
		isReplayedRefactoring= false;
		RefactoringContribution refactoringContribution= RefactoringCore.getRefactoringContribution(id);
		if (refactoringContribution == null) {
			Debugger.debugWarning("Failed to get refactoring contribution for id: " + id);
			return;
		}
		RefactoringDescriptor refactoringDescriptor= refactoringContribution.createDescriptor(id, project.isEmpty() ? null : project, "Recorded refactoring", "", arguments, flags);
		replayRefactoring(refactoringDescriptor);
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
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

	protected abstract void replayRefactoring(RefactoringDescriptor refactoringDescriptor) throws CoreException;

}
