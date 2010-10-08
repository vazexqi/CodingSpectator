package edu.illinois.codingspectator.codingtracker;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.history.IRefactoringExecutionListener;
import org.eclipse.ltk.core.refactoring.history.RefactoringExecutionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public class CodeChangeTracker implements IStartup, ISelectionListener, ITextListener, IRefactoringExecutionListener, IResourceChangeListener {

	private final CodeChangeTracker trackerInstance;

	private final Logger logger;

	private IFile currentFile= null;

	Set<ISourceViewer> listenedViewers= new HashSet<ISourceViewer>();

	private volatile boolean isRefactoring= false;

	public CodeChangeTracker() {
		logger= new Logger();
		trackerInstance= this;
	}

	@Override
	public void earlyStartup() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(trackerInstance, IResourceChangeEvent.POST_CHANGE);
		//TODO: !!!Sometimes it is registered later than the user changes some text. As a result, text change is lost.
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				IWorkbenchWindow activeWorkbenchWindow= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (activeWorkbenchWindow == null) {
					throw new RuntimeException("Failed to get the active workbench window");
				}
				activeWorkbenchWindow.getSelectionService().addSelectionListener(trackerInstance);
				IWorkbenchPage activePage= activeWorkbenchWindow.getActivePage();
				if (activePage != null) {
					selectionChanged(activePage.getActivePart(), activePage.getSelection());
				}
				RefactoringCore.getHistoryService().addExecutionListener(trackerInstance);
			}
		});
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
//		System.out.println("NOTIFIED!");
		if (part instanceof JavaEditor) {
			JavaEditor javaEditor= (JavaEditor)part;
			IEditorInput editorInput= javaEditor.getEditorInput();
			if (!(editorInput instanceof FileEditorInput)) {
				throw new RuntimeException("JavaEditor's input is not FileEditorInput:" + javaEditor.getTitle());
			}
			IFile newFile= ((FileEditorInput)editorInput).getFile();
			if (!newFile.equals(currentFile)) {
				currentFile= newFile;
				System.out.println("File:\"" + currentFile.getFullPath().toOSString() + "\"");
			}
			ISourceViewer sourceViewer= javaEditor.getViewer();
			if (!listenedViewers.contains(sourceViewer)) {
				System.out.println("Viewer added: " + sourceViewer);
				sourceViewer.addTextListener(trackerInstance);
				listenedViewers.add(sourceViewer);
			}
		}
	}

	@Override
	public void textChanged(TextEvent event) {
		DocumentEvent documentEvent= event.getDocumentEvent();
		if (documentEvent != null && !isRefactoring) {
			logger.logTextEvent(event, currentFile);
		}
	}

	@Override
	public void executionNotification(RefactoringExecutionEvent event) {
		int eventType= event.getEventType();
		if (eventType == RefactoringExecutionEvent.ABOUT_TO_PERFORM || eventType == RefactoringExecutionEvent.ABOUT_TO_REDO ||
				eventType == RefactoringExecutionEvent.ABOUT_TO_UNDO) {
			isRefactoring= true;
			System.out.println("START REFACTORING");
		} else {
			isRefactoring= false;
			logger.logRefactoringExecutionEvent(event);
		}
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		System.out.println("RESOURCE CHANGED");
	}

}
