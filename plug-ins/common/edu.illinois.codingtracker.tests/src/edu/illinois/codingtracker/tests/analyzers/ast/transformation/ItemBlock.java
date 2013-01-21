/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.ast.transformation;

import java.util.TreeMap;

import edu.illinois.codingtracker.helpers.Configuration;
import edu.illinois.codingtracker.operations.ast.InferredUnknownTransformationOperation;


/**
 * @author Stas Negara
 * 
 */
class ItemBlock {

	private final long startTimestamp;

	private final boolean isFirst;

	private final TreeMap<Long, Item> items= new TreeMap<Long, Item>();


	public ItemBlock(long startTimestamp, boolean isFirst) {
		this.startTimestamp= startTimestamp;
		this.isFirst= isFirst;
	}

	public boolean isFirst() {
		return isFirst;
	}

	public TreeMap<Long, Item> getItems() {
		return items;
	}

	public boolean canBePartOfBlock(InferredUnknownTransformationOperation operation) {
		return Math.abs(operation.getTime() - startTimestamp) <= Configuration.miningMaxBlockSize;
	}

	public void addToBlock(InferredUnknownTransformationOperation operation) {
		if (!canBePartOfBlock(operation)) {
			throw new RuntimeException("Tried to add operation that can not be part of the block!");
		}
		items.put(operation.getTransformationID(), new LongItem(operation.getTransformationKindID()));
	}

}
