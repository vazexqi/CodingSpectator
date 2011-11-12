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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.IUndoManager;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.internal.core.refactoring.history.DefaultRefactoringDescriptor;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringContributionManager;

import edu.illinois.codingtracker.helpers.Configuration;
import edu.illinois.codingtracker.helpers.Debugger;
import edu.illinois.codingtracker.jdt.project.manipulation.JavaProjectHelper;
import edu.illinois.codingtracker.operations.JavaProjectsUpkeeper;
import edu.illinois.codingtracker.operations.OperationLexer;
import edu.illinois.codingtracker.operations.OperationSymbols;
import edu.illinois.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingtracker.operations.UserOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings({ "rawtypes", "restriction" })
public class NewStartedRefactoringOperation extends UserOperation {

	public static enum RefactoringMode {
		PERFORM, UNDO, REDO
	};

	private static final int shouldAlwaysReplayOffset= 50;

	private static Set<Long> unperformedRefactorings= new HashSet<Long>();

	//Shows whether this is an older refactoring, which should always be replayed to reproduce the code changes.
	private boolean shouldAlwaysReplay= false; //this flag is serialized/deserialized together with refactoringMode

	private RefactoringMode refactoringMode;

	private String id;

	private String project;

	private int flags;

	//TreeMap is required for the deterministic behavior that is expected by the tests
	private final Map<String, String> arguments= new TreeMap<String, String>();


	public NewStartedRefactoringOperation() {
		super();
	}

	public NewStartedRefactoringOperation(RefactoringMode refactoringMode, RefactoringDescriptor refactoringDescriptor) {
		super(getRefactoringTimeStamp(refactoringDescriptor));
		this.refactoringMode= refactoringMode;
		id= refactoringDescriptor.getID();
		project= refactoringDescriptor.getProject();
		flags= refactoringDescriptor.getFlags();
		initializeArguments(getRefactoringArguments(refactoringDescriptor));
	}

	/**
	 * This constructor should be used only for conversions performed by postprocessors.
	 * 
	 * @param refactoringMode
	 * @param id
	 * @param project
	 * @param flags
	 * @param arguments
	 * @param timestamp
	 */
	public NewStartedRefactoringOperation(boolean shouldAlwaysReplay, RefactoringMode refactoringMode, String id, String project, int flags,
											Map<String, String> arguments, long timestamp) {
		super(timestamp);
		this.shouldAlwaysReplay= shouldAlwaysReplay;
		this.refactoringMode= refactoringMode;
		this.id= id;
		this.project= project;
		this.flags= flags;
		initializeArguments(arguments);
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.NEW_REFACTORING_STARTED_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "[new] Started refactoring";
	}

	public boolean getShouldAlwaysReplay() {
		return shouldAlwaysReplay;
	}

	public RefactoringMode getRefactoringMode() {
		return refactoringMode;
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
	 * that this method is used only by postprocessors, it should be OK.
	 * 
	 * @return
	 */
	public Map<String, String> getArguments() {
		return arguments;
	}

	public boolean isRename() {
		return id.startsWith("org.eclipse.jdt.ui.rename.");
	}

	private static long getRefactoringTimeStamp(RefactoringDescriptor refactoringDescriptor) {
		long refactoringDescriptorTimeStamp= refactoringDescriptor.getTimeStamp();
		return refactoringDescriptorTimeStamp == -1 ? System.currentTimeMillis() : refactoringDescriptorTimeStamp;
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
		int modeOrdinal= refactoringMode.ordinal();
		textChunk.append(shouldAlwaysReplay ? shouldAlwaysReplayOffset + modeOrdinal : modeOrdinal);
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
		int offsetModeOrdinal= operationLexer.readInt();
		if (offsetModeOrdinal >= shouldAlwaysReplayOffset) {
			offsetModeOrdinal-= shouldAlwaysReplayOffset;
			shouldAlwaysReplay= true;
		}
		refactoringMode= RefactoringMode.values()[offsetModeOrdinal];
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
		if (shouldAlwaysReplay || Configuration.isInTestMode) { //replay only older refactorings or if in the test mode
			isReplayedRefactoring= true;
			switch (refactoringMode) {
				case PERFORM:
					RefactoringDescriptor refactoringDescriptor= createRefactoringDescriptor();
					if (refactoringDescriptor != null) {
						replayPerform(refactoringDescriptor);
					}
					break;
				case UNDO:
					replayUndo();
					break;
				case REDO:
					replayRedo();
					break;
			}
		}
	}

	private RefactoringDescriptor createRefactoringDescriptor() throws CoreException {
		RefactoringContribution refactoringContribution= RefactoringCore.getRefactoringContribution(id);
		if (refactoringContribution == null) {
			Debugger.debugWarning("Failed to get refactoring contribution for id: " + id);
			return null;
		}
		Map<String, String> refactoringArguments= arguments;
		//Special preprocessing for rename source folder refactoring
		if ("org.eclipse.jdt.ui.rename.source.folder".equals(id)) {
			//Add an argument 'path' that is expected by Eclipse
			refactoringArguments= new TreeMap<String, String>(); //create a new map to keep the original map intact
			refactoringArguments.putAll(arguments);
			refactoringArguments.put("path", "/" + project + refactoringArguments.get("input"));
			//Add this folder to the build path (i.e. make it to be "source folder")
			IJavaProject javaProject= JavaProjectsUpkeeper.findOrCreateJavaProject(project);
			JavaProjectHelper.addSourceContainer(javaProject, refactoringArguments.get("input").substring((1)));
		}
		return refactoringContribution.createDescriptor(id, project.isEmpty() ? null : project, "Recorded refactoring", "", refactoringArguments, flags);
	}

	private void replayPerform(RefactoringDescriptor refactoringDescriptor) throws CoreException {
		try {
			//TODO: This is a temporary hack. It is required to overcome the problem that sometimes Eclipse does not finish updating 
			//program's structure yet, and thus, the refactoring can not be properly initialized (i.e. the refactored element is not found).
			//Find a better solution, e.g. listen for some Eclipse "refreshing" process to complete.
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			//do nothing
		}
		RefactoringStatus initializationStatus= new RefactoringStatus();
		Refactoring refactoring= refactoringDescriptor.createRefactoring(initializationStatus);
		if (initializationStatus.hasError()) {
			Debugger.debugWarning("Failed to initialize a refactoring from its descriptor: " + refactoringDescriptor);
			unperformedRefactorings.add(getTime());
			return;
		}
		//This remove is needed to ensure that multiple replays in the same run do not overlap
		unperformedRefactorings.remove(getTime());
		PerformRefactoringOperation performRefactoringOperation= new PerformRefactoringOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS);
		performRefactoringOperation.run(null);
		checkExecutionStatus(refactoring.getName(), performRefactoringOperation);
	}

	private void checkExecutionStatus(String refactoringName, PerformRefactoringOperation performRefactoringOperation) {
		if (performRefactoringOperation.getConditionStatus().hasFatalError()) {
			throw new RuntimeException("Failed to check preconditions of refactoring: " + refactoringName);
		}
		if (performRefactoringOperation.getValidationStatus().hasFatalError()) {
			throw new RuntimeException("Failed to validate refactoring: " + refactoringName);
		}
	}

	private void replayUndo() throws CoreException {
		if (!unperformedRefactorings.contains(getTime())) {
			getRefactoringUndoManager().performUndo(null, null);
		}
	}

	private void replayRedo() throws CoreException {
		if (!unperformedRefactorings.contains(getTime())) {
			getRefactoringUndoManager().performRedo(null, null);
		}
	}

	private IUndoManager getRefactoringUndoManager() {
		return RefactoringCore.getUndoManager();
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("Should always replay: " + shouldAlwaysReplay + "\n");
		sb.append("Refactoring mode: " + refactoringMode + "\n");
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
