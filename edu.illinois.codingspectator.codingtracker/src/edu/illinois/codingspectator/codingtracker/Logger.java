package edu.illinois.codingspectator.codingtracker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringExecutionEvent;
import org.eclipse.ltk.internal.core.refactoring.history.DefaultRefactoringDescriptor;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringContributionManager;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryService;

/**
 * 
 * @author Stas Negara
 * 
 *         TODO: Replace e.printStack by outputting to the error log.
 * 
 */
@SuppressWarnings("restriction")
public class Logger {

	private BufferedWriter logFileWriter= null;

	public static final IPath watchedDirectory= Platform.getStateLocation(
			Platform.getBundle(Messages.Logger_LTKBundleName));

	private static final String featureVersion= RefactoringHistoryService.getFeatureVersion().toString();

	private static final String LOGFILE_NAME= Messages.Logger_CodeChangesFileName;

	private IFile lastEditedFile= null;

	private static final String ESCAPE_SYMBOL= "~"; //$NON-NLS-1$

	private static final String DELIMETER_SYMBOL= "#"; //$NON-NLS-1$

	private static final String FILE_EDIT_SYMBOL= "f"; //$NON-NLS-1$

	private static final String TEXT_CHANGE_SYMBOL= "t"; //$NON-NLS-1$

	private static final String REFACTORING_PERFORMED_SYMBOL= "p"; //$NON-NLS-1$

	private static final String REFACTORING_UNDONE_SYMBOL= "u"; //$NON-NLS-1$

	private static final String REFACTORING_REDONE_SYMBOL= "r"; //$NON-NLS-1$


	public Logger() {
		//TODO: Upon integration change the location of the file
		IPath logFilePath= watchedDirectory.append(featureVersion).append(LOGFILE_NAME);
		File file= new File(logFilePath.toOSString());
		file.getParentFile().mkdirs();
		try {
			file.createNewFile();
			logFileWriter= new BufferedWriter(new FileWriter(file, true));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void logTextEvent(TextEvent event, IFile editedFile) {
		System.out.println("Replaced text:\"" + (event.getReplacedText() == null ? "" : event.getReplacedText()) + //$NON-NLS-1$ //$NON-NLS-2$
				"\", new text:\"" + event.getText() + "\", offset=" + event.getOffset() + ", length=" + event.getLength()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (!editedFile.equals(lastEditedFile)) {
			lastEditedFile= editedFile;
			logEditedFile();
		}
		StringBuffer textChange= new StringBuffer();
		textChange.append(TEXT_CHANGE_SYMBOL);
		String escapedReplacedText= event.getReplacedText() == null ? "" : escapeString(event.getReplacedText()); //$NON-NLS-1$
		textChange.append(escapedReplacedText).append(DELIMETER_SYMBOL);
		textChange.append(escapeString(event.getText())).append(DELIMETER_SYMBOL);
		textChange.append(event.getOffset()).append(DELIMETER_SYMBOL);
		textChange.append(event.getLength()).append(DELIMETER_SYMBOL);
		textChange.append(System.currentTimeMillis()).append(DELIMETER_SYMBOL);
		System.out.println("Change: " + textChange.toString()); //$NON-NLS-1$
		log(textChange);
	}

	private void logEditedFile() {
		StringBuffer fileEdit= new StringBuffer();
		fileEdit.append(FILE_EDIT_SYMBOL);
		fileEdit.append(lastEditedFile.getFullPath().toOSString()).append(DELIMETER_SYMBOL);
		System.out.println("File edit: " + fileEdit.toString()); //$NON-NLS-1$
		log(fileEdit);
	}

	@SuppressWarnings("rawtypes")
	public void logRefactoringExecutionEvent(RefactoringExecutionEvent event) {
		RefactoringDescriptorProxy refactoringDescriptorProxy= event.getDescriptor();
		RefactoringDescriptor refactoringDescriptor= refactoringDescriptorProxy.requestDescriptor(new NullProgressMonitor());
		System.out.println("Refactoring descriptor id: " + refactoringDescriptor.getID()); //$NON-NLS-1$
		System.out.println("Project: " + refactoringDescriptor.getProject()); //$NON-NLS-1$
		System.out.println("Flags: " + refactoringDescriptor.getFlags()); //$NON-NLS-1$
		System.out.println("Timestamp: " + refactoringDescriptor.getTimeStamp()); //$NON-NLS-1$
		Map arguments= getRefactoringArguments(refactoringDescriptor);
		Set keys= arguments.keySet();
		for (Object key : keys) {
			Object value= arguments.get(key);
			System.out.println("Argument \"" + key + "\" = \"" + value + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		StringBuffer refactoringInfo= new StringBuffer();
		switch (event.getEventType()) {
			case RefactoringExecutionEvent.PERFORMED:
				refactoringInfo.append(REFACTORING_PERFORMED_SYMBOL);
				break;
			case RefactoringExecutionEvent.REDONE:
				refactoringInfo.append(REFACTORING_REDONE_SYMBOL);
				break;
			case RefactoringExecutionEvent.UNDONE:
				refactoringInfo.append(REFACTORING_UNDONE_SYMBOL);
				break;
			default:
				throw new RuntimeException("Refactoring event type unrecognized: " + event.getEventType()); //$NON-NLS-1$
		}
		refactoringInfo.append(escapeString(refactoringDescriptor.getID())).append(DELIMETER_SYMBOL);
		refactoringInfo.append(escapeString(refactoringDescriptor.getProject())).append(DELIMETER_SYMBOL);
		refactoringInfo.append(refactoringDescriptor.getFlags()).append(DELIMETER_SYMBOL);
		refactoringInfo.append(keys.size()).append(DELIMETER_SYMBOL);
		for (Object key : keys) {
			Object value= arguments.get(key);
			refactoringInfo.append(escapeString(key.toString())).append(DELIMETER_SYMBOL);
			refactoringInfo.append(escapeString(value.toString())).append(DELIMETER_SYMBOL);
		}
		refactoringInfo.append(refactoringDescriptor.getTimeStamp()).append(DELIMETER_SYMBOL);
		System.out.println("Refactoring info: " + refactoringInfo.toString()); //$NON-NLS-1$
		log(refactoringInfo);
	}

	@SuppressWarnings("rawtypes")
	private Map getRefactoringArguments(RefactoringDescriptor descriptor) {
		Map arguments= null;
		RefactoringContribution contribution= RefactoringContributionManager.getInstance().getRefactoringContribution(descriptor.getID());
		if (contribution != null)
			arguments= contribution.retrieveArgumentMap(descriptor);
		else if (descriptor instanceof DefaultRefactoringDescriptor)
			arguments= ((DefaultRefactoringDescriptor)descriptor).getArguments();
		if (arguments == null) {
			throw new RuntimeException("Failed to get arguments of the descriptor: " + descriptor.getID()); //$NON-NLS-1$
		}
		return arguments;
	}

	private String escapeString(String str) {
		String tempString= str.replace(ESCAPE_SYMBOL, ESCAPE_SYMBOL + ESCAPE_SYMBOL);
		return tempString.replace(DELIMETER_SYMBOL, ESCAPE_SYMBOL + DELIMETER_SYMBOL);
	}

	private void log(CharSequence text) {
		try {
			logFileWriter.append(text);
			logFileWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
