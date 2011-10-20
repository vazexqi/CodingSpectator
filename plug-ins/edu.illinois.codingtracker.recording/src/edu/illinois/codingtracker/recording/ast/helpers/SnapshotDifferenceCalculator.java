/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.recording.ast.helpers;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.contentmergeviewer.ITokenComparator;
import org.eclipse.compare.contentmergeviewer.TokenComparator;
import org.eclipse.compare.internal.MergeViewerContentProvider;
import org.eclipse.compare.internal.merge.DocumentMerger;
import org.eclipse.compare.internal.merge.DocumentMerger.Diff;
import org.eclipse.compare.internal.merge.DocumentMerger.IDocumentMergerInput;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.ui.compare.JavaTokenComparator;
import org.eclipse.jdt.internal.ui.compare.JavaTokenComparator.ITokenComparatorFactory;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

import edu.illinois.codingtracker.operations.textchanges.PerformedTextChangeOperation;



/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public class SnapshotDifferenceCalculator {

	private static final CompareConfiguration compareConfiguration= new CompareConfiguration();

	private static IDocument leftDocument;

	private static IDocument rightDocument;

	private static int accumulatedOffsetDelta;

	private static long timestamp;

	private static final List<PerformedTextChangeOperation> snapshotDifference= new LinkedList<PerformedTextChangeOperation>();

	@SuppressWarnings("rawtypes")
	public static List<PerformedTextChangeOperation> getSnapshotDifference(String currentSnapshot, String newSnapshot, long timestamp) {
		snapshotDifference.clear();
		accumulatedOffsetDelta= 0;
		leftDocument= new Document(currentSnapshot);
		rightDocument= new Document(newSnapshot);
		SnapshotDifferenceCalculator.timestamp= timestamp;
		DocumentMerger documentMerger= createDocumentMerger();
		try {
			documentMerger.doDiff();
		} catch (CoreException e) {
			throw new RuntimeException("Could not perform document diff: ", e);
		}
		Iterator changesIterator= documentMerger.changesIterator();
		while (changesIterator.hasNext()) {
			collectDiff((Diff)changesIterator.next());
		}
		if (!leftDocument.get().equals(rightDocument.get())) {
			throw new RuntimeException("Left document does not match right document after applying diffs!");
		}
		return snapshotDifference;
	}

	@SuppressWarnings("rawtypes")
	private static void collectDiff(Diff diff) {
		if (diff.hasChildren()) {
			Iterator childIterator= diff.childIterator();
			while (childIterator.hasNext()) {
				collectDiff((Diff)childIterator.next());
			}
		} else {
			//Apply directly only the leaf diffs (i.e. with no children).
			Position leftPosition= diff.getPosition(MergeViewerContentProvider.LEFT_CONTRIBUTOR);
			Position rightPosition= diff.getPosition(MergeViewerContentProvider.RIGHT_CONTRIBUTOR);
			int leftDocumentOffset= leftPosition.getOffset() + accumulatedOffsetDelta;
			try {
				String replacedText= leftDocument.get(leftDocumentOffset, leftPosition.getLength());
				String newText= rightDocument.get(rightPosition.getOffset(), rightPosition.getLength());
				leftDocument.replace(leftDocumentOffset, leftPosition.getLength(), newText);
				DocumentEvent documentEvent= new DocumentEvent(leftDocument, leftDocumentOffset, leftPosition.getLength(), newText);
				snapshotDifference.add(new PerformedTextChangeOperation(documentEvent, replacedText, timestamp));
			} catch (BadLocationException e) {
				throw new RuntimeException(e);
			}
			accumulatedOffsetDelta+= rightPosition.getLength() - leftPosition.getLength();
		}
	}

	private static DocumentMerger createDocumentMerger() {
		DocumentMerger documentMerger= new DocumentMerger(new IDocumentMergerInput() {

			@Override
			public IDocument getDocument(char contributor) {
				switch (contributor) {
					case MergeViewerContentProvider.LEFT_CONTRIBUTOR:
						return leftDocument;
					case MergeViewerContentProvider.RIGHT_CONTRIBUTOR:
						return rightDocument;
					default:
						return null;
				}
			}

			@Override
			public Position getRegion(char contributor) {
				switch (contributor) {
					case MergeViewerContentProvider.LEFT_CONTRIBUTOR:
						return new Position(0, leftDocument.getLength());
					case MergeViewerContentProvider.RIGHT_CONTRIBUTOR:
						return new Position(0, rightDocument.getLength());
					default:
						return null;
				}
			}

			@Override
			public boolean isIgnoreAncestor() {
				return true;
			}

			@Override
			public boolean isThreeWay() {
				return false;
			}

			@Override
			public CompareConfiguration getCompareConfiguration() {
				return compareConfiguration;
			}

			@Override
			public ITokenComparator createTokenComparator(String s) {
				return new JavaTokenComparator(s, new ITokenComparatorFactory() {
					@Override
					public ITokenComparator createTokenComparator(String text) {
						return new TokenComparator(text);
					}
				});
			}

			@Override
			public boolean isHunkOnLeft() {
				return false;
			}

			@Override
			public int getHunkStart() {
				return 0;
			}

			@Override
			public boolean isPatchHunk() {
				return false;
			}

			@Override
			public boolean isShowPseudoConflicts() {
				return false;
			}

			@Override
			public boolean isPatchHunkOk() {
				return false;
			}
		});
		return documentMerger;
	}

}
