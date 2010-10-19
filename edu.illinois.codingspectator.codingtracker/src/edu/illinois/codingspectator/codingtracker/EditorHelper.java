/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.team.internal.ui.mapping.ModelCompareEditorInput;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

/**
 * 
 * @author Stas Negara
 * @author Mohsen Vakilian - Extracted this class from CodeChangeTracker
 * 
 */
public class EditorHelper {

	public static boolean isConflictEditor(IEditorPart editor) {
		if (!(editor instanceof CompareEditor)) {
			return false;
		}
		//TODO: Maybe some other inputs (not of a conflict editor) are good for tracking and are not ModelCompareEditorInput
		if (((CompareEditor)editor).getEditorInput() instanceof ModelCompareEditorInput) {
			return false;
		}
		return true;
	}

	static String getConflictEditorInitialContent(CompareEditor compareEditor) {
		CompareEditorInput compareEditorInput= (CompareEditorInput)compareEditor.getEditorInput();
		ICompareInput compareInput= (ICompareInput)compareEditorInput.getCompareResult();
		ResourceNode resourceNode= (ResourceNode)compareInput.getLeft();
		return new String(resourceNode.getContent());
	}

	public static String getConflictEditorID(CompareEditor compareEditor) {
		String compareEditorString= compareEditor.toString();
		return compareEditorString.substring(compareEditorString.lastIndexOf('@') + 1);
	}

	public static IFile getEditorJavaFile(CompareEditor compareEditor) {
		IFile javaFile= null;
		IEditorInput editorInput= compareEditor.getEditorInput();
		if (editorInput instanceof CompareEditorInput) {
			CompareEditorInput compareEditorInput= (CompareEditorInput)editorInput;
			Object compareResult= compareEditorInput.getCompareResult();
			if (compareResult instanceof ICompareInput) {
				ICompareInput compareInput= (ICompareInput)compareResult;
				ITypedElement leftTypedElement= compareInput.getLeft();
				if (leftTypedElement instanceof ResourceNode) {
					ResourceNode resourceNode= (ResourceNode)leftTypedElement;
					IResource resource= resourceNode.getResource();
					if (resource instanceof IFile) {
						IFile file= (IFile)resource;
						if (FileProperties.isJavaFile(file)) {
							javaFile= file;
						}
					}
				}
			}
		}
		return javaFile;
	}

	public static IFile getEditorJavaFile(AbstractDecoratedTextEditor editor) {
		IFile javaFile= null;
		IEditorInput editorInput= editor.getEditorInput();
		if (editorInput instanceof FileEditorInput) {
			IFile file= ((FileEditorInput)editorInput).getFile();
			if (FileProperties.isJavaFile(file)) {
				javaFile= file;
			}
		}
		return javaFile;
	}

	public static ISourceViewer getEditorSourceViewer(CompareEditor compareEditor) {
		ISourceViewer sourceViewer= null;
		IEditorInput editorInput= compareEditor.getEditorInput();
		if (editorInput instanceof CompareEditorInput) {
			CompareEditorInput compareEditorInput= (CompareEditorInput)editorInput;
			Viewer contentViewer= compareEditorInput.getContentViewer();
			if (contentViewer instanceof TextMergeViewer) {
				sourceViewer= ((TextMergeViewer)contentViewer).getLeftViewer();
			}
		}
		return sourceViewer;
	}

	public static ISourceViewer getEditorSourceViewer(AbstractDecoratedTextEditor editor) {
		return editor.getHackedViewer();
	}

}
