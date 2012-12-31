/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.ast.transformation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


/**
 * 
 * @author Stas Negara
 * 
 */
public class Transaction {

	private final int transactionID;

	private Map<String, Set<Long>> transactionItemInstances= new TreeMap<String, Set<Long>>();

	private Map<String, Set<Long>> removedDuplicatedItemInstances= new HashMap<String, Set<Long>>();


	public Transaction(int transactionID) {
		this.transactionID= transactionID;
	}

	public void addItemInstance(String item, long instanceID) {
		Set<Long> instanceIDs= transactionItemInstances.get(item);
		if (instanceIDs == null) {
			instanceIDs= new HashSet<Long>();
			transactionItemInstances.put(item, instanceIDs);
		}
		instanceIDs.add(instanceID);
	}

	public Set<Long> getInstancesForItem(char item) {
		return transactionItemInstances.get(String.valueOf(item));
	}

	public void addRemovedDuplicatedItemInstances(String itemSet, Set<Long> newRemovedDuplicatedItemInstances) {
		Set<Long> currentRemovedItemInstances= removedDuplicatedItemInstances.get(itemSet);
		if (currentRemovedItemInstances == null) {
			currentRemovedItemInstances= new HashSet<Long>();
			removedDuplicatedItemInstances.put(itemSet, currentRemovedItemInstances);
		}
		currentRemovedItemInstances.addAll(newRemovedDuplicatedItemInstances);
	}

	public void printItemSetInstances(String itemSet) {
		Set<Long> removedInstanceIDs= removedDuplicatedItemInstances.get(itemSet);
		for (char item : itemSet.toCharArray()) {
			for (long itemInstanceID : getInstancesForItem(item)) {
				String marker= "";
				if (removedInstanceIDs != null && removedInstanceIDs.contains(itemInstanceID)) {
					marker= "~";
				}
				System.out.print(marker + itemInstanceID + marker + ", ");
			}
			System.out.print("||");
		}
		System.out.println();
	}

	private List<Set<Long>> getValidItemSetInstances(String itemSet) {
		Set<Long> removedInstanceIDs= removedDuplicatedItemInstances.get(itemSet);
		List<Set<Long>> validItemSetInstances= new LinkedList<Set<Long>>();
		for (char item : itemSet.toCharArray()) {
			Set<Long> validInstanceIDs= new HashSet<Long>();
			for (long itemInstanceID : getInstancesForItem(item)) {
				if (removedInstanceIDs == null || !removedInstanceIDs.contains(itemInstanceID)) {
					validInstanceIDs.add(itemInstanceID);
				}
			}
			validItemSetInstances.add(validInstanceIDs);
		}
		return validItemSetInstances;
	}

	public int getFrequency(String itemSet) {
		return SetHelper.getMinimumSetSize(getValidItemSetInstances(itemSet));
	}

	public void removeDuplicatedInstances(Transaction subsequentTransaction, String itemSet) {
		if (transactionID >= subsequentTransaction.transactionID) {
			throw new RuntimeException("Should remove duplicates oly versus the subsequent transaction!");
		}
		if (subsequentTransaction.transactionID - transactionID > 1) {
			return; //Duplicates can occur only in adjacent transactions.
		}
		List<Set<Long>> thisItemSetInstances= getValidItemSetInstances(itemSet);
		List<Set<Long>> subsequentItemSetInstances= subsequentTransaction.getValidItemSetInstances(itemSet);
		if (shouldPrioritizeFirst(thisItemSetInstances, subsequentItemSetInstances)) {
			subsequentTransaction.addRemovedDuplicatedItemInstances(itemSet, SetHelper.getAllInstancesAsSet(thisItemSetInstances));
		} else {
			addRemovedDuplicatedItemInstances(itemSet, SetHelper.getAllInstancesAsSet(subsequentItemSetInstances));
		}
	}

	private boolean shouldPrioritizeFirst(List<Set<Long>> firstItemSetInstances, List<Set<Long>> secondItemSetInstances) {
		List<Set<Long>> firstFromSecond= SetHelper.subtractItemSetInstances(secondItemSetInstances, firstItemSetInstances);
		List<Set<Long>> secondFromFirst= SetHelper.subtractItemSetInstances(firstItemSetInstances, secondItemSetInstances);

		//First, check if one itemset completely subsumes the other one, and prioritize the subsuming itemset.
		if (SetHelper.getAllInstancesAsSet(firstFromSecond).size() == 0) {
			return true;
		}
		if (SetHelper.getAllInstancesAsSet(secondFromFirst).size() == 0) {
			return false;
		}

		//If neither itemset is subsuming, prioritize the itemset with the highest minimum set size, which means
		//the highest number of itemset repetitions.
		int firstMinimumSetSize= SetHelper.getMinimumSetSize(firstItemSetInstances);
		int secondMinimumSetSize= SetHelper.getMinimumSetSize(secondItemSetInstances);
		if (firstMinimumSetSize > secondMinimumSetSize) {
			return true;
		}
		if (firstMinimumSetSize < secondMinimumSetSize) {
			return false;
		}

		//If both itemsets have the same minimum size, prioritize the one with the best difference, i.e., 
		//the difference with the highest minimum set size.
		int minimumSizeFirstFromSecond= SetHelper.getMinimumSetSize(firstFromSecond);
		int minimumSizeSecondFromFirst= SetHelper.getMinimumSetSize(secondFromFirst);
		if (minimumSizeFirstFromSecond > minimumSizeSecondFromFirst) {
			return true;
		}
		if (minimumSizeFirstFromSecond < minimumSizeSecondFromFirst) {
			return false;
		}

		//If differences have the same minimum set sizes, prioritize the set with more instances.
		int firstAllInstancesSize= SetHelper.getAllInstancesAsSet(firstItemSetInstances).size();
		int secondAllInstancesSize= SetHelper.getAllInstancesAsSet(secondItemSetInstances).size();
		if (firstAllInstancesSize > secondAllInstancesSize) {
			return true;
		}
		if (firstAllInstancesSize < secondAllInstancesSize) {
			return false;
		}

		//No more heuristics to apply, just prioritize the earlier (first) transaction.
		return true;
	}

}
