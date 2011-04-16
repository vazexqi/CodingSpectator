/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.refactoringproblems.logger;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 
 * @author Mohsen Vakilian
 * @author Balaji Ambresh Rajkumar
 * 
 */
class StringOutputStream extends OutputStream {

	StringBuilder sb= new StringBuilder();

	@Override
	public void write(int b) throws IOException {
		sb.append((char)b);
	}

	@Override
	public String toString() {
		return sb.toString();
	}

}
