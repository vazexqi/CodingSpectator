/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.ast.transformation;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeSet;

import edu.illinois.codingtracker.tests.analyzers.ast.transformation.helpers.TransactionsFrequencyPair;


/**
 * 
 * @author Stas Negara
 * 
 */
public class ResultsComparator implements Comparator<Entry<TreeSet<Item>, TransactionsFrequencyPair>> {

	enum ComparisonStrategy {
		FREQUENCY, SIZE, FREQUENCY_AND_SIZE
	};

	private final ComparisonStrategy strategy;


	public ResultsComparator(ComparisonStrategy strategy) {
		this.strategy= strategy;
	}

	@Override
	public int compare(Entry<TreeSet<Item>, TransactionsFrequencyPair> entry1, Entry<TreeSet<Item>, TransactionsFrequencyPair> entry2) {
		TreeSet<Item> itemSet1= entry1.getKey();
		TreeSet<Item> itemSet2= entry2.getKey();
		if (itemSet1.equals(itemSet2)) {
			return 0; //Should return 0 only for the same itemset to avoid squashing together different itemsets.
		}

		if (strategy == ComparisonStrategy.FREQUENCY) {
			int frequencyComparison= compareValues(entry1.getValue().getFrequency(), entry2.getValue().getFrequency());
			if (frequencyComparison != 0) {
				return frequencyComparison;
			}
			int sizeComparison= compareSize(itemSet1, itemSet2);
			if (sizeComparison != 0) {
				return sizeComparison;
			}
		} else if (strategy == ComparisonStrategy.SIZE) {
			int sizeComparison= compareSize(itemSet1, itemSet2);
			if (sizeComparison != 0) {
				return sizeComparison;
			}
			int frequencyComparison= compareValues(entry1.getValue().getFrequency(), entry2.getValue().getFrequency());
			if (frequencyComparison != 0) {
				return frequencyComparison;
			}
		} else {
			int frequencyAndSizeComparison= compareValues(entry1.getValue().getFrequency() * itemSet1.size(), entry2.getValue().getFrequency() * itemSet2.size());
			if (frequencyAndSizeComparison != 0) {
				return frequencyAndSizeComparison;
			}
			int sizeComparison= compareSize(itemSet1, itemSet2);
			if (sizeComparison != 0) {
				return sizeComparison;
			}
		}

		//Contents should be compared only when itemsets are of the same size.
		return compareContents(itemSet1, itemSet2);
	}

	private int compareValues(int value1, int value2) {
		if (value1 < value2) {
			return 1;
		}
		if (value1 > value2) {
			return -1;
		}
		return 0;
	}

	private int compareSize(TreeSet<Item> itemSet1, TreeSet<Item> itemSet2) {
		if (itemSet1.size() < itemSet2.size()) {
			return 1;
		}
		if (itemSet1.size() > itemSet2.size()) {
			return -1;
		}
		return 0;
	}

	private int compareContents(TreeSet<Item> itemSet1, TreeSet<Item> itemSet2) {
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

}
