/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.ast.transformation;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.illinois.codingtracker.tests.analyzers.ast.transformation.helpers.SetMapHelper;


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


	public RemainingItemsComparator(TreeSet<Item> baseItemSet, Set<Integer> baseItemSetTransactions) {
		this.baseItemSet= baseItemSet;
		this.baseItemSetTransactions= baseItemSetTransactions;
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
		Frequency frequency1= getItemFrequency(item1);
		Frequency frequency2= getItemFrequency(item2);
		int comparisonResult= frequency1.compareTo(frequency2);
		if (comparisonResult != 0) {
			return comparisonResult;
		}
		//Should never return 0 for different items to avoid squashing them!
		return item1.compareTo(item2);
	}

	public Frequency getItemFrequency(Item item) {
		return getItemFrequency(item, null);
	}

	private Frequency getItemFrequency(Item item, Set<Integer> subsetTransactionIDs) {
		if (baseItemSet.contains(item)) { //TODO: Disable this check when not testing to speed up a little bit.
			throw new RuntimeException("The base item set already contains the item for which the frequency is requested!");
		}
		baseItemSet.add(item);
		Frequency frequency;
		if (subsetTransactionIDs != null) {
			frequency= UnknownTransformationMiner.getSubsetFrequency(baseItemSet, subsetTransactionIDs);
		} else {
			frequency= UnknownTransformationMiner.getFrequency(baseItemSet, getCommonTransactionIDs(item));
		}
		baseItemSet.remove(item);
		return frequency;
	}

	public Set<Integer> getCommonTransactionIDs(Item item) {
		Set<Integer> commonTransactionIDs= cachedCommonTransactionIDs.get(item);
		if (commonTransactionIDs == null) {
			Set<Integer> itemTransactions= UnknownTransformationMiner.getInputItemTransactions(item);
			commonTransactionIDs= baseItemSet.isEmpty() ? itemTransactions : SetMapHelper.intersectTreeSets(baseItemSetTransactions, itemTransactions);
			cachedCommonTransactionIDs.put(item, commonTransactionIDs);
		}
		return commonTransactionIDs;
	}

	public ItemPairStatus compareItems(Item item1, Item item2) {
		Set<Integer> commonTransactionIDs1= getCommonTransactionIDs(item1);
		Set<Integer> commonTransactionIDs2= getCommonTransactionIDs(item2);
		if (commonTransactionIDs1.equals(commonTransactionIDs2)) {
			Frequency frequency1= getItemFrequency(item1);
			Frequency frequency2= getItemFrequency(item2);
			if (frequency1.isEqualTo(frequency2)) {
				return ItemPairStatus.EQUIVALENT;
			} else if (frequency2.isNotLessPowerfulThan(frequency1)) {
				return ItemPairStatus.SECOND_PREVAILS;
			} else if (frequency1.isNotLessPowerfulThan(frequency2)) {
				//TODO: Disable this check when not testing to speed up a little bit.
				throw new RuntimeException("Ordering of items is wrong!");
			}
		} else if (commonTransactionIDs2.containsAll(commonTransactionIDs1) &&
					getItemFrequency(item2, commonTransactionIDs1).isNotLessPowerfulThan(getItemFrequency(item1))) {
			return ItemPairStatus.SECOND_PREVAILS;
		}
		return ItemPairStatus.ORTHOGONAL;
	}
}
