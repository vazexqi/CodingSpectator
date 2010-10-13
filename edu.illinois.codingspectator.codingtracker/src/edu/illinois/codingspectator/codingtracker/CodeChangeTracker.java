/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.commands.operations.TriggeredOperations;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.history.IRefactoringExecutionListener;
import org.eclipse.ltk.core.refactoring.history.RefactoringExecutionEvent;
import org.eclipse.ltk.internal.core.refactoring.UndoableOperation2ChangeAdapter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public class CodeChangeTracker implements ISelectionListener, ITextListener, IRefactoringExecutionListener,
											IResourceChangeListener, IPartListener, IOperationHistoryListener {

	private static CodeChangeTracker trackerInstance;

	private final Logger logger;

	private IFile currentFile= null;

	private AbstractDecoratedTextEditor currentEditor= null;

	ISourceViewer listenedViewer= null;

	private volatile boolean isRefactoring= false;

	private volatile boolean isUndoing= false;

	private volatile boolean isRedoing= false;
	
	private boolean isPartListenerRegistered = false;
	
	private volatile IWorkbenchWindow activeWorkbenchWindow= null;

	private Set<IFile> dirtyFiles= Collections.synchronizedSet(new HashSet<IFile>());

	public static CodeChangeTracker getInstance(){
		if (trackerInstance == null){
			trackerInstance = new CodeChangeTracker();
		}
		return trackerInstance;
	}
	
	private CodeChangeTracker() {
		logger= new Logger();
	}

	public void start() {
		System.out.println("Early startup");
		ResourcesPlugin.getWorkspace().addResourceChangeListener(trackerInstance, IResourceChangeEvent.POST_CHANGE);
		OperationHistoryFactory.getOperationHistory().addOperationHistoryListener(trackerInstance);
		RefactoringCore.getHistoryService().addExecutionListener(trackerInstance);
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				activeWorkbenchWindow= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (activeWorkbenchWindow == null) {
					Exception e= new RuntimeException();
					Logger.logExceptionToErrorLog(e, Messages.CodeChangeTracker_FailedToGetActiveWorkbenchWindow);
				}
				activeWorkbenchWindow.getSelectionService().addSelectionListener(trackerInstance);
				IWorkbenchPage activePage= activeWorkbenchWindow.getActivePage();
				if (activePage != null) {
					selectionChanged(activePage.getActivePart(), activePage.getSelection());
					activePage.addPartListener(trackerInstance);
					isPartListenerRegistered = true;
				}
			}
		});
		if (!isPartListenerRegistered){
			Display.getDefault().asyncExec(new Runnable(){

				@Override
				public void run() {
					//TODO: Is it too heavy-weight? Did not notice any additional lag even on a slow machine.  
					while (!isPartListenerRegistered){
						IWorkbenchPage activePage= activeWorkbenchWindow.getActivePage();
						if (activePage != null){
							activePage.addPartListener(trackerInstance);				
							isPartListenerRegistered = true;
						}						
					}
				}				
			});
		}
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
//		System.out.println("NOTIFIED!");
		if (part instanceof AbstractDecoratedTextEditor) {
			AbstractDecoratedTextEditor editor= (AbstractDecoratedTextEditor)part;
			IEditorInput editorInput= editor.getEditorInput();
			if (!(editorInput instanceof FileEditorInput)) {
				Exception e= new RuntimeException();
				Logger.logExceptionToErrorLog(e, Messages.CodeChangeTracker_WrongJavaEditorInput +
													editor.getTitle());
			}
			IFile newFile= ((FileEditorInput)editorInput).getFile();
			//Look only for Java files
			if (newFile.getFileExtension().equals("java")){  //$NON-NLS-1$
				if (!newFile.equals(currentFile)) {
					currentFile= newFile;
					currentEditor = editor;
					//Or, alternatively:
					//FileBuffers.getTextFileBufferManager().getTextFileBuffer(currentFile.getFullPath(), LocationKind.IFILE);				
					System.out.println("File:\"" + currentFile.getFullPath().toPortableString() + "\"");
				}
				ISourceViewer sourceViewer= editor.getHackedViewer();
				if (listenedViewer != null) {
					listenedViewer.removeTextListener(trackerInstance);
				}
				listenedViewer= sourceViewer;
				listenedViewer.addTextListener(trackerInstance);
			}
		}
	}

	@Override
	public void textChanged(TextEvent event) {
		DocumentEvent documentEvent= event.getDocumentEvent();
		if (documentEvent != null && !isRefactoring) {
			dirtyFiles.add(currentFile);
			logger.logTextEvent(event, currentFile, isUndoing, isRedoing);
		}
	}

	@Override
	public void executionNotification(RefactoringExecutionEvent event) {
		int eventType= event.getEventType();
		if (eventType == RefactoringExecutionEvent.ABOUT_TO_PERFORM || eventType == RefactoringExecutionEvent.ABOUT_TO_REDO ||
				eventType == RefactoringExecutionEvent.ABOUT_TO_UNDO) {
			isRefactoring= true;
			logger.logRefactoringStarted();
		} else {
			isRefactoring= false;
			logger.logRefactoringExecutionEvent(event);
		}
	}

	@Override
	public void historyNotification(OperationHistoryEvent event) {
		int eventType= event.getEventType();
		if (eventType == OperationHistoryEvent.ABOUT_TO_UNDO) {
			isUndoing= true;
		} else {
			isUndoing= false;
		}
		if (eventType == OperationHistoryEvent.ABOUT_TO_REDO) {
			isRedoing= true;
		} else {
			isRedoing= false;
		}
		if (eventType == OperationHistoryEvent.UNDONE || eventType == OperationHistoryEvent.REDONE){
			if (currentEditor.isDirty()){
				dirtyFiles.add(currentFile);
			}else{
				dirtyFiles.remove(currentFile);
			}
		}
		if (eventType == OperationHistoryEvent.ABOUT_TO_EXECUTE || (eventType == OperationHistoryEvent.ABOUT_TO_REDO) ||
				eventType == OperationHistoryEvent.ABOUT_TO_UNDO) {
			IUndoableOperation undoableOperation= event.getOperation();
			if (undoableOperation instanceof TriggeredOperations) {
				IUndoableOperation triggeringOperation= ((TriggeredOperations)undoableOperation).getTriggeringOperation();
				if (triggeringOperation instanceof UndoableOperation2ChangeAdapter) {
					Object[] affectedObjects= ((UndoableOperation2ChangeAdapter)triggeringOperation).getAllAffectedObjects();
					if (affectedObjects != null) {
						for (Object affectedObject : affectedObjects) {
							if (affectedObject instanceof CompilationUnit) {
								IResource resource= ((CompilationUnit)affectedObject).getResource();
								if (resource instanceof IFile) { //Could it be something else?
									IFile file= (IFile)resource;
//									System.out.println("File to be modified:" + file.getFullPath().toPortableString());
									logger.ensureIsKnownFile(file);
								}
							}
						}
					}
				}
			}

		}
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta= event.getDelta();
		if (delta != null) { //why could it be null?
			final Set<IFile> changedJavaFiles= new HashSet<IFile>();
			final Set<IFile> removedJavaFiles= new HashSet<IFile>();
			final Set<IFile> svnChangedJavaFiles= new HashSet<IFile>();
			try {
				delta.accept(new IResourceDeltaVisitor() {

					@Override
					public boolean visit(IResourceDelta delta) throws CoreException {
						IResource resource= delta.getResource();
						if (resource.getType() == IResource.FILE) {
							IFile file= (IFile)resource;
							String fileExtension= file.getFileExtension();
							if (fileExtension != null) {
								if (fileExtension.equals("java")) { //$NON-NLS-1$
									if ((delta.getKind() == IResourceDelta.CHANGED) && ((delta.getFlags() & IResourceDelta.CONTENT) != 0)) {
										changedJavaFiles.add(file);
									} else if (delta.getKind() == IResourceDelta.REMOVED) {
										removedJavaFiles.add(file);
									}
								} else if (fileExtension.equals("svn-base") && //$NON-NLS-1$
										(delta.getKind() == IResourceDelta.CHANGED) && ((delta.getFlags() & IResourceDelta.CONTENT) != 0)) {
									String fileName= file.getName();
									if (fileName.endsWith(".java.svn-base")) { //$NON-NLS-1$
										IPath fileFullPath= file.getFullPath();
										String parentDir= fileFullPath.segment(fileFullPath.segmentCount() - 2);
										if (parentDir.equals("text-base")) { //$NON-NLS-1$
											String javaSourceFileName= fileName.substring(0, fileName.lastIndexOf(".")); //$NON-NLS-1$
											IPath javaSourceFilePath= fileFullPath.removeLastSegments(3).append(javaSourceFileName);
											IFile javaSourceFile= ResourcesPlugin.getWorkspace().getRoot().getFile(javaSourceFilePath);
											svnChangedJavaFiles.add(javaSourceFile);
										}
									}
								}
							}
						}
						return true;
					}
				}, IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS); //to see changes in .svn folder
			} catch (CoreException e) {
				Logger.logExceptionToErrorLog(e, Messages.CodeChangeTracker_FailedToVisitResourceDelta);
			}
			final Set<IFile> savedJavaFiles= new HashSet<IFile>();
			final Set<IFile> externallyModifiedJavaFiles= new HashSet<IFile>();
			final Set<IFile> updatedJavaFiles= new HashSet<IFile>();
			final Set<IFile> committedJavaFiles= new HashSet<IFile>();
			for (IFile file : svnChangedJavaFiles) {
				if (changedJavaFiles.contains(file)) {
					updatedJavaFiles.add(file); //if both the java file and its svn storage have changed, then its an update
				} else {
					committedJavaFiles.add(file); //if only svn storage of a java file has changed, its a commit
				}
			}
			for (IFile file : changedJavaFiles) {
				if (!updatedJavaFiles.contains(file)) { //updated files are neither saved nor externally modified
					if (isRefactoring || dirtyFiles.contains(file)){  
						savedJavaFiles.add(file);						
					}else{
						externallyModifiedJavaFiles.add(file);
					}					
				}
				dirtyFiles.remove(file);
			}
			dirtyFiles.removeAll(removedJavaFiles);
			logger.logSavedFiles(savedJavaFiles, isRefactoring);
			logger.logExternallyModifiedFiles(externallyModifiedJavaFiles);
			logger.logUpdatedFiles(updatedJavaFiles);
			logger.logCommittedFiles(committedJavaFiles);
			removedJavaFiles.addAll(updatedJavaFiles); //updated files become unknown (like removed)
			removedJavaFiles.addAll(externallyModifiedJavaFiles); //externally modified files become unknown
			logger.removeKnownFiles(removedJavaFiles);
		}
	}

	@Override
	public void partActivated(IWorkbenchPart part) {
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
	}

	@Override
	public void partClosed(IWorkbenchPart part) {
		if (part instanceof AbstractDecoratedTextEditor) {
			AbstractDecoratedTextEditor editor= (AbstractDecoratedTextEditor)part;
			IEditorInput editorInput= editor.getEditorInput();
			if (!(editorInput instanceof FileEditorInput)) {
				Exception e= new RuntimeException();
				Logger.logExceptionToErrorLog(e, Messages.CodeChangeTracker_WrongJavaEditorInput +
													editor.getTitle());
			}
			IFile closedFile= ((FileEditorInput)editorInput).getFile();
			dirtyFiles.remove(closedFile);
			logger.logClosedFile(closedFile);
		}
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
	}

}
