package edu.illinois.codingtracker.codeskimmer;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;

import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.files.EditedFileOperation;
import edu.illinois.codingtracker.operations.textchanges.TextChangeOperation;

/**
 * CodeFilter extends Filter and targets specified ranges of text.
 * 
 * @author Connor Simmons
 * 
 */
public class CodeFilter extends Filter {

	private IntegerRange selectionRange;
	private String selectionFilePath;
	private long selectionTimestamp;

	private Hashtable<UserOperation, Boolean> matchHash;

	public CodeFilter(int selectionStart, int selectionEnd,
			String selectionFilePath, UserOperation operationAtSelection) {
		this.selectionRange = new IntegerRange(selectionStart, selectionEnd);
		this.selectionFilePath = selectionFilePath;

		if (operationAtSelection == null) {
			selectionTimestamp = Long.MAX_VALUE;
		} else {
			selectionTimestamp = operationAtSelection.getTime();
		}
	}

	/**
	 * Initializes this filter for the given list of operations
	 * 
	 * @param operations
	 *            A list of User Operations that this filter will match against.
	 */
	@Override
	public void init(List<UserOperation> operations) {
		matchHash = new Hashtable<UserOperation, Boolean>();
		generateMatchHash(operations);
	}

	/**
	 * Checks a given operation to see if it matches this filter. This should
	 * only be called once the filter has been initialized
	 * 
	 * @param operation
	 *            the operation we check for a match.
	 * @return whether or not the operation matches this filter
	 */
	@Override
	public boolean matchesOperation(UserOperation operation) {
		if (matchHash.get(operation) != null && matchHash.get(operation)) {
			return true;
		}

		return false;
	}

	/**
	 * Returns true if the text changed in the given operation overlaps the
	 * integer range. The text range is measured by the offset from the
	 * beginning of the file.
	 * 
	 * @param tOp
	 *            the text change user operation
	 * @param selectRange
	 *            The integer range we are checking the operation range against
	 * @return true if the ranges overlap or false otherwise
	 */
	private boolean operationOverlapsSelectRange(TextChangeOperation tOp,
			IntegerRange selectRange) {

		IntegerRange opStartRange = new IntegerRange(tOp.getOffset(),
				tOp.getOffset() + tOp.getReplacedText().length());
		IntegerRange opEndRange = new IntegerRange(tOp.getOffset(),
				tOp.getOffset() + tOp.getNewText().length());

		return (selectRange.start < selectRange.end)
				&& (selectRange.overlaps(opStartRange) || selectRange
						.overlaps(opEndRange));
	}

	/**
	 * Called during init. This function generates a dictionary with true values
	 * for each UserOperation matched by this filter. The function works by
	 * iterating forward and backward through the full list of operations. At
	 * each step the range of interest is altered based on the user operations.
	 * 
	 * @param userOperations	the full list of user operations
	 */
	private void generateMatchHash(List<UserOperation> userOperations) {
		// figure out where in this list the user made the selection
		int selectionPointIndex = 0;
		while (selectionPointIndex < userOperations.size()
				&& userOperations.get(selectionPointIndex).getTime() < selectionTimestamp) {
			selectionPointIndex++;
		}

		// some pre-processing - we generate a stack of edited file paths
		// so we can quickly check if a user operation is changing a certain
		// file
		ArrayList<String> filePaths = new ArrayList<String>();
		int selectionPointFileIndex = 0;
		for (int i = 0; i < userOperations.size(); i++) {
			UserOperation cur = userOperations.get(i);
			if (cur instanceof EditedFileOperation) {
				filePaths.add(((EditedFileOperation) cur).getResourcePath());
				if (cur.getTime() < selectionTimestamp) {
					selectionPointFileIndex = filePaths.size();
				}
			}
		}

		if (filePaths.size() > 0) {
			/*
			 * FIRST ITERATE BACKWARD STARTING AT THE SELECTION POINT
			 */

			// last item should be the "final" selection range
			IntegerRange curRange = new IntegerRange(selectionRange.start,
					selectionRange.end);

			// iterate backward over all earlier user operations
			ListIterator<UserOperation> li = userOperations
					.listIterator(selectionPointIndex);
			int filePathsIndex = selectionPointFileIndex - 1;
			String currentFilePath = "";
			if (filePathsIndex >= 0) {
				currentFilePath = filePaths.get(filePathsIndex);
			}
			while (li.hasPrevious()) {
				UserOperation op = li.previous();

				// we only care if this is an operation that changes the text
				// and this is the file we care about
				if (op instanceof TextChangeOperation
						&& currentFilePath.equals(selectionFilePath)) {
					TextChangeOperation tOp = (TextChangeOperation) op;

					int opOffset = tOp.getOffset();
					int opOldLength = tOp.getReplacedText().length();
					int opNewLength = tOp.getNewText().length();

					// check if this offset overlaps the current range
					if (operationOverlapsSelectRange(tOp, curRange)) {
						matchHash.put(op, true);
					}

					// now we update the top of our selection range stack
					// think about it. it works.
					if (opOffset < curRange.start) {
						curRange.start += opOldLength - opNewLength;
					}
					if (opOffset < curRange.end) {
						curRange.end += opOldLength - opNewLength;
					}

				} else if (op instanceof EditedFileOperation
						&& filePathsIndex > 0) {
					currentFilePath = filePaths.get(--filePathsIndex);
				}
			}

			/*
			 * NOW ITERATE FORWARD STARTING AT SELECTION POINT
			 */
			curRange = new IntegerRange(selectionRange.start,
					selectionRange.end);
			li = userOperations.listIterator(selectionPointIndex);
			filePathsIndex = selectionPointFileIndex - 1;
			currentFilePath = "";
			if (filePathsIndex >= 0) {
				currentFilePath = filePaths.get(filePathsIndex);
			}
			while (li.hasNext()) {
				UserOperation op = li.next();

				// we only care if this is an operation that changes the text //
				// and this is the file we care about
				if (op instanceof TextChangeOperation
						&& currentFilePath.equals(selectionFilePath)) {
					TextChangeOperation tOp = (TextChangeOperation) op;

					int opOffset = tOp.getOffset();
					int opOldLength = tOp.getReplacedText().length();
					int opNewLength = tOp.getNewText().length();

					// check if this offset overlaps the current range
					if (operationOverlapsSelectRange(tOp, curRange)) {
						matchHash.put(op, true);
					}

					// now we update the top of our selection range stack //
					// think about it. it works.
					if (opOffset < curRange.start) {
						curRange.start += opNewLength - opOldLength;
					}
					if (opOffset < curRange.end) {
						curRange.end += opNewLength - opOldLength;
					}

				} else if (op instanceof EditedFileOperation
						&& filePathsIndex < filePaths.size() - 1) {
					currentFilePath = filePaths.get(++filePathsIndex);
				}
			}

		}
	}

	/**
	 * A simple class used to keep track of ranges of integers and compare them.
	 *
	 */
	private class IntegerRange {
		public int start;
		public int end;

		public IntegerRange(int start, int end) {
			this.start = start;
			this.end = end;
		}

		/*
		 * Returns true if the other range overlaps with this one (inclusive)
		 */
		public boolean overlaps(IntegerRange otherRange) {
			return this.start <= otherRange.end && otherRange.start <= this.end;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			IntegerRange other = (IntegerRange) obj;
			if (end != other.end)
				return false;
			if (start != other.start)
				return false;
			return true;
		}
	}

	@Override
	public String toString() {
		return "\"" + selectionFilePath + "\", " + selectionRange.start + "-"
				+ selectionRange.end;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CodeFilter other = (CodeFilter) obj;
		if (selectionFilePath == null) {
			if (other.selectionFilePath != null)
				return false;
		} else if (!selectionFilePath.equals(other.selectionFilePath))
			return false;
		if (selectionRange == null) {
			if (other.selectionRange != null)
				return false;
		} else if (!selectionRange.equals(other.selectionRange))
			return false;
		return true;
	}
}
