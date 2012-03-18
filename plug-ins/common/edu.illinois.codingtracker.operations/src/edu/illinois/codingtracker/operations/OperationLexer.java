/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations;

/**
 * 
 * @author Stas Negara
 * 
 */
public class OperationLexer {

	private static final String ESCAPE_SYMBOL= "~";

	public static final String DELIMETER_SYMBOL= "#";

	private static final String ESCAPED_ESCAPE_SYMBOL= ESCAPE_SYMBOL + ESCAPE_SYMBOL;

	public static final String ESCAPED_DELIMETER_SYMBOL= ESCAPE_SYMBOL + DELIMETER_SYMBOL;

	private final String operationsRecord;

	private int currentIndex= 0;

	private char currentOperationSymbol;

	public OperationLexer(String operationsRecord) {
		this.operationsRecord= operationsRecord;
	}

	public boolean hasNextOperation() {
		if (currentIndex >= operationsRecord.length()) {
			return false;
		}
		return true;
	}

	public void startNewOperation() {
		currentOperationSymbol= operationsRecord.charAt(currentIndex++);
	}

	public char getCurrentOperationSymbol() {
		return currentOperationSymbol;
	}

	private String getNextLexeme(boolean shouldAdvance) {
		int delimeterIndex= getCurrentDelimiterIndex();
		String lexeme= operationsRecord.substring(currentIndex, delimeterIndex);
		if (shouldAdvance) {
			currentIndex= delimeterIndex + 1;
		}
		return unescapeString(lexeme);
	}

	private int getCurrentDelimiterIndex() {
		int delimeterIndex= operationsRecord.indexOf(DELIMETER_SYMBOL, currentIndex);
		while (isEscapedDelimeter(delimeterIndex)) {
			delimeterIndex= operationsRecord.indexOf(DELIMETER_SYMBOL, delimeterIndex + 1);
		}
		return delimeterIndex;
	}

	public String readString() {
		return getNextLexeme(true);
	}

	public int readInt() {
		return Integer.parseInt(getNextLexeme(true));
	}

	public long readLong() {
		return Long.parseLong(getNextLexeme(true));
	}

	public boolean readBoolean() {
		return readInt() == 1 ? true : false;
	}

	public boolean readOptionalBoolean() {
		String lexeme= getNextLexeme(false);
		if (lexeme.equals("1") || lexeme.equals("0")) {
			return readBoolean();
		}
		return false; //If there is no boolean to read, return false. 
	}

	private boolean isEscapedDelimeter(int delimeterIndex) {
		int escapeSymbolCount= 0;
		for (int index= delimeterIndex - 1; index >= 0; index--) {
			if (ESCAPE_SYMBOL.charAt(0) == operationsRecord.charAt(index)) {
				escapeSymbolCount++;
			} else {
				break;
			}
		}
		if (escapeSymbolCount > 0 && escapeSymbolCount % 2 > 0) {
			return true;
		}
		return false;
	}

	public static String escapeString(String str) {
		String tempString= str.replace(ESCAPE_SYMBOL, ESCAPED_ESCAPE_SYMBOL);
		return tempString.replace(DELIMETER_SYMBOL, ESCAPED_DELIMETER_SYMBOL);
	}

	public static String unescapeString(String str) {
		String tempString= str.replace(ESCAPED_ESCAPE_SYMBOL, ESCAPE_SYMBOL);
		return tempString.replace(ESCAPED_DELIMETER_SYMBOL, DELIMETER_SYMBOL);
	}

}
