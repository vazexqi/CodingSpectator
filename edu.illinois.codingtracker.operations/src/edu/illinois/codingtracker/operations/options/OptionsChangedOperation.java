/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.options;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import edu.illinois.codingtracker.operations.OperationLexer;
import edu.illinois.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingtracker.operations.UserOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class OptionsChangedOperation extends UserOperation {

	//TreeMap is required for the deterministic behavior that is expected by the tests
	protected final Map<String, String> options= new TreeMap<String, String>();

	public OptionsChangedOperation() {
		super();
	}

	public OptionsChangedOperation(Map<String, String> options) {
		this.options.putAll(options);
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		textChunk.append(options.size());
		for (Entry<String, String> optionsEntry : options.entrySet()) {
			textChunk.append(optionsEntry.getKey());
			textChunk.append(optionsEntry.getValue());
		}
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		int optionsCount= operationLexer.readInt();
		for (int i= 0; i < optionsCount; i++) {
			options.put(operationLexer.readString(), operationLexer.readString());
		}
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("Options count: " + options.size() + "\n");
		for (Entry<String, String> optionsEntry : options.entrySet()) {
			sb.append("Key: " + optionsEntry.getKey() + "\n");
			sb.append("Value: " + optionsEntry.getValue() + "\n");
		}
		sb.append(super.toString());
		return sb.toString();
	}

}
