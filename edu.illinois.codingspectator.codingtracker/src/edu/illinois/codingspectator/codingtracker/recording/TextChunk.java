/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.recording;

/**
 * 
 * @author Stas Negara
 * 
 */
public class TextChunk implements CharSequence {

	private static final String ESCAPE_SYMBOL= "~";

	private static final String DELIMETER_SYMBOL= "#";

	private final StringBuffer text;

	public TextChunk(String chunkType) {
		text= new StringBuffer();
		text.append(chunkType);
	}

	public void append(Object obj) {
		this.append(obj.toString());
	}

	public void append(String str) {
		if (str == null) {
			str= "";
		}
		text.append(escapeString(str)).append(DELIMETER_SYMBOL);
	}

	public void append(int num) {
		text.append(num).append(DELIMETER_SYMBOL);
	}

	public void append(long num) {
		text.append(num).append(DELIMETER_SYMBOL);
	}

	private String escapeString(String str) {
		String tempString= str.replace(ESCAPE_SYMBOL, ESCAPE_SYMBOL + ESCAPE_SYMBOL);
		return tempString.replace(DELIMETER_SYMBOL, ESCAPE_SYMBOL + DELIMETER_SYMBOL);
	}

	@Override
	public char charAt(int index) {
		return text.charAt(index);
	}

	@Override
	public int length() {
		return text.length();
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return text.subSequence(start, end);
	}

	@Override
	public String toString() {
		return text.toString();
	}

}
