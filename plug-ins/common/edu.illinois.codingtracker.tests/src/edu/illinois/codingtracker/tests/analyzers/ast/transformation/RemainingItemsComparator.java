/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.ast.transformation;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.illinois.codingtracker.helpers.Configuration;


/**
 * 
 * @author Stas Negara
 * 
 */
public class RemainingItemsComparator implements Comparator<Item> {

	public enum ItemPairStatus {
		ORTHOGONAL, EQUIVALENT, SECOND_PREVAILS
	};

	private final TreeSet<Item> baseItemSet;

	private final Set<Integer> baseItemSetTransactions;

	private final Map<Item, Set<Integer>> cachedCommonTransactionIDs= new HashMap<Item, Set<Integer>>();


	public RemainingItemsComparator(TreeSet<Item> baseItemSet) {
		this.baseItemSet= baseItemSet;
		baseItemSetTransactions= UnknownTransformationMiner.getResultItemSetTransactions(baseItemSet);
	}

	public TreeSet<Item> getBaseItemSet() {
		return baseItemSet;
	}

	@Override
	public int compare(Item item1, Item item2) {
		Set<Integer> commonTransactionIDs1= getCommonTransactionIDs(item1);
		Set<Integer> commonTransactionIDs2= getCommonTransactionIDs(item2);
		if (commonTransactionIDs1.size() < commonTransactionIDs2.size()) {
			return -1;
		}
		if (commonTransactionIDs1.size() > commonTransactionIDs2.size()) {
			return 1;
		}
		return compareFrequencies(item1, item2);
	}

	private int compareFrequencies(Item item1, Item item2) {
		int frequency1= getItemFrequency(item1);
		int frequency2= getItemFrequency(item2);
		if (frequency1 < frequency2) {
			return -1;
		} else if (frequency1 > frequency2) {
			return 1;
		} else {
			return item1.compareTo(item2); //Should never return 0 for different items to avoid squashing them!
		}
	}

	private int getItemFrequency(Item item) {
		baseItemSet.add(item);
		int frequency= UnknownTransformationMiner.getFrequency(baseItemSet, getCommonTransactionIDs(item));
		baseItemSet.remove(item);
		return frequency;
	}

	public Set<Integer> getCommonTransactionIDs(Item item) {
		Set<Integer> commonTransactionIDs= cachedCommonTransactionIDs.get(item);
		if (commonTransactionIDs == null) {
			Set<Integer> itemTransactions= UnknownTransformationMiner.getInputItemTransactions(item);
			commonTransactionIDs= baseItemSet.isEmpty() ? itemTransactions : SetHelper.intersectTreeSets(baseItemSetTransactions, itemTransactions);
			cachedCommonTransactionIDs.put(item, commonTransactionIDs);
		}
		return commonTransactionIDs;
	}

	public boolean isFrequent(Item item) {
		return getItemFrequency(item) >= Configuration.miningFrequencyThreshold;
	}

	public ItemPairStatus compareItems(Item item1, Item item2) {
		Set<Integer> commonTransactionIDs1= getCommonTransactionIDs(item1);
		Set<Integer> commonTransactionIDs2= getCommonTransactionIDs(item2);
		if (commonTransactionIDs1.equals(commonTransactionIDs2)) {
			int frequency1= getItemFrequency(item1);
			int frequency2= getItemFrequency(item2);
			if (frequency1 == frequency2) {
				return ItemPairStatus.EQUIVALENT;
			} else if (frequency1 < frequency2) {
				return ItemPairStatus.SECOND_PREVAILS;
			}
			throw new RuntimeException("Ordering of items is wrong!");
		}
		if (commonTransactionIDs2.containsAll(commonTransactionIDs1)) {
			if (getItemFrequency(item1) <= getItemFrequency(item2)) {
				return ItemPairStatus.SECOND_PREVAILS;
			}
		}
		return ItemPairStatus.ORTHOGONAL;
	}

}
