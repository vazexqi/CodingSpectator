package edu.illinois.codingspectator.refactoringproblems.parser;

/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
import java.io.IOException;
import java.io.Reader;

/**
 * Allows composition of two generic readers. Allows reading and resetting but doesn't allow marking
 * or skipping. For usage example see {@link RefactoringProblemsLogDeserializer}
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class CompositeReader extends Reader {

	private Reader firstInput;

	private Reader secondInput;

	public CompositeReader(Reader firstInput, Reader secondInput) {
		this.firstInput= firstInput;
		this.secondInput= secondInput;
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		synchronized (lock) {
			int remaining;

			// Attempt to read from first
			int amountReadFromFirst= firstInput.read(cbuf, off, len);

			if (amountReadFromFirst == -1) {
				amountReadFromFirst= 0;
			}
			remaining= len - amountReadFromFirst;

			return amountReadFromFirst + secondInput.read(cbuf, off + amountReadFromFirst, remaining);
		}
	}

	@Override
	public void close() throws IOException {
		firstInput.close();
		secondInput.close();
	}

	@Override
	public long skip(long n) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean ready() throws IOException {
		return firstInput.ready() && secondInput.ready();
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	@Override
	public void mark(int readAheadLimit) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void reset() throws IOException {
		firstInput.reset();
		secondInput.reset();
	}

}
