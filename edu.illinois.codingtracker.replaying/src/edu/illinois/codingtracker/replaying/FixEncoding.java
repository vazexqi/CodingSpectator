/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.replaying;

import java.io.File;
import java.io.IOException;

import edu.illinois.codingtracker.helpers.ResourceHelper;

/**
 * This class fixes encoding in the operation sequences produced by the old versions of
 * CodingTracker, which did not use UTF-8 to store user operations.
 * 
 * @author Stas Negara
 * 
 */
public class FixEncoding {

	public static void main(String[] args) {
		//TODO: Might require specifying a particular encoding to read the file, by default it is UTF-8.
		String originalText= ResourceHelper.readFileContent(new File(args[0]));
		try {
			File outputFile= new File(args[0] + ".fixed");
			if (outputFile.exists()) {
				throw new RuntimeException("Output file already exists: " + outputFile.getName());
			}
			ResourceHelper.writeFileContent(outputFile, normalizeEncoding(originalText), false);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String normalizeEncoding(String text) {
		StringBuffer stringBuffer= new StringBuffer();
		int i= 0;
		while (i < text.length()) {
			char currentChar= text.charAt(i);
			if (!isASCIICharacter(currentChar)) {
				stringBuffer.append("?");
				i+= 2;
			} else {
				stringBuffer.append(currentChar);
				i++;
			}
		}
		return stringBuffer.toString();
	}

	private static boolean isASCIICharacter(char character) {
		byte byteValue= (byte)character;
		return byteValue >= 0 && byteValue <= 127;
	}


}
