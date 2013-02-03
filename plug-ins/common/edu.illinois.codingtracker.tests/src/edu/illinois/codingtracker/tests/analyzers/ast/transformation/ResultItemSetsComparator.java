/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.ast.transformation;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;


/**
 * 
 * @author Stas Negara
 * 
 */
public class ResultItemSetsComparator implements Comparator<TreeSet<Item>> {

	//This collection is shared with the miner.
	private final Map<TreeSet<Item>, Frequency> itemSetFrequencies;


	public ResultItemSetsComparator(Map<TreeSet<Item>, Frequency> itemSetFrequencies) {
		this.itemSetFrequencies= itemSetFrequencies;
	}

	@Override
	public int compare(TreeSet<Item> itemSet1, TreeSet<Item> itemSet2) {
		int frequency1= getItemSetFrequency(itemSet1);
		int frequency2= getItemSetFrequency(itemSet2);
		if (frequency1 < frequency2) {
			return 1;
		}
		if (frequency1 > frequency2) {
			return -1;
		}
		if (itemSet1.equals(itemSet2)) {
			return 0; //Should return 0 only for the same itemset to avoid squashing together different itemsets.
		}
		return compareSize(itemSet1, itemSet2);
	}

	private int compareSize(TreeSet<Item> itemSet1, TreeSet<Item> itemSet2) {
		if (itemSet1.size() < itemSet2.size()) {
			return 1;
		}
		if (itemSet1.size() > itemSet2.size()) {
			return -1;
		}
		Iterator<Item> it1= itemSet1.iterator();
		Iterator<Item> it2= itemSet2.iterator();
		while (it1.hasNext()) {
			int itemComparison= it1.next().compareTo(it2.next());
			if (itemComparison != 0) {
				return itemComparison;
			}
		}
		throw new RuntimeException("Should not reach here!");
	}

	private int getItemSetFrequency(TreeSet<Item> itemSet) {
		Frequency frequency= itemSetFrequencies.get(itemSet);
		if (frequency == null) {
			throw new RuntimeException("Could not get frequency of an itemset!");
		}
		return frequency.getOverallFrequency();
	}

}
