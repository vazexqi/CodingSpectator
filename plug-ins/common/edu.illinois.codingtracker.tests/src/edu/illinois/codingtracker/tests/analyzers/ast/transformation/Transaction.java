/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.ast.transformation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


/**
 * 
 * @author Stas Negara
 * 
 */
public class Transaction {

	private final int transactionID;

	private Map<Item, Set<Long>> itemInstances= new HashMap<Item, Set<Long>>();

	private Map<Set<Item>, Set<Long>> removedDuplicatedItemInstances= new HashMap<Set<Item>, Set<Long>>();


	public Transaction(int transactionID) {
		this.transactionID= transactionID;
	}

	public void addItemInstance(Item item, long instanceID) {
		Set<Long> instanceIDs= itemInstances.get(item);
		if (instanceIDs == null) {
			instanceIDs= new TreeSet<Long>();
			itemInstances.put(item, instanceIDs);
		}
		instanceIDs.add(instanceID);
	}

	public void addRemovedDuplicatedItemInstances(TreeSet<Item> itemSet, Set<Long> newRemovedDuplicatedItemInstances) {
		Set<Long> currentRemovedItemInstances= removedDuplicatedItemInstances.get(itemSet);
		if (currentRemovedItemInstances == null) {
			currentRemovedItemInstances= new HashSet<Long>();
			removedDuplicatedItemInstances.put(itemSet, currentRemovedItemInstances);
		}
		currentRemovedItemInstances.addAll(newRemovedDuplicatedItemInstances);
	}

	public void printItemSetInstances(TreeSet<Item> itemSet) {
		Set<Long> removedInstanceIDs= removedDuplicatedItemInstances.get(itemSet);
		for (Item item : itemSet) {
			for (long itemInstanceID : itemInstances.get(item)) {
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

	private List<Set<Long>> getValidItemSetInstances(TreeSet<Item> itemSet) {
		Set<Long> removedInstanceIDs= removedDuplicatedItemInstances.get(itemSet);
		List<Set<Long>> validItemSetInstances= new LinkedList<Set<Long>>();
		for (Item item : itemSet) {
			Set<Long> validInstanceIDs= new TreeSet<Long>();
			for (long itemInstanceID : itemInstances.get(item)) {
				if (removedInstanceIDs == null || !removedInstanceIDs.contains(itemInstanceID)) {
					validInstanceIDs.add(itemInstanceID);
				}
			}
			validItemSetInstances.add(validInstanceIDs);
		}
		return validItemSetInstances;
	}

	public int getFrequency(TreeSet<Item> itemSet) {
		return SetHelper.getMinimumSetSize(getValidItemSetInstances(itemSet));
	}

	public void removeDuplicatedInstances(Transaction subsequentTransaction, TreeSet<Item> itemSet) {
		if (transactionID >= subsequentTransaction.transactionID) {
			throw new RuntimeException("Should remove duplicates oly versus the subsequent transaction!");
		}
		if (subsequentTransaction.transactionID - transactionID > 1) {
			return; //Duplicates can occur only in adjacent transactions.
		}

		Set<Long> toRemoveFromThis= new HashSet<Long>();
		Set<Long> toRemoveFromSubsequent= new HashSet<Long>();
		collectInstancesToRemove(getValidItemSetInstances(itemSet), subsequentTransaction.getValidItemSetInstances(itemSet), toRemoveFromThis, toRemoveFromSubsequent);

		subsequentTransaction.addRemovedDuplicatedItemInstances(itemSet, toRemoveFromSubsequent);
		if (SetHelper.getMinimumSetSize(subsequentTransaction.getValidItemSetInstances(itemSet)) == 0) {
			//If the subsequent transaction is destroyed, remove all duplicates from it.
			subsequentTransaction.addRemovedDuplicatedItemInstances(itemSet, toRemoveFromThis);
		} else {
			addRemovedDuplicatedItemInstances(itemSet, toRemoveFromThis);
		}
	}

	private void collectInstancesToRemove(List<Set<Long>> thisItemSetInstances, List<Set<Long>> subsequentItemSetInstances, Set<Long> toRemoveFromThis, Set<Long> toRemoveFromSubsequent) {
		int thisMinimumSetSize= SetHelper.getMinimumSetSize(thisItemSetInstances);
		Iterator<Set<Long>> subsequentItemSetInstancesIterator= subsequentItemSetInstances.iterator();
		for (Set<Long> thisInstances : thisItemSetInstances) {
			Set<Long> subsequentInstances= subsequentItemSetInstancesIterator.next();
			int count= 0;
			for (long instanceID : thisInstances) {
				count++;
				if (subsequentInstances.contains(instanceID)) { //Is a duplicated instance.
					if (count <= thisMinimumSetSize) { //Part of the minimum core, remove from the subsequent.
						toRemoveFromSubsequent.add(instanceID);
					} else { //Is not part of the minimum core, remove from this.
						toRemoveFromThis.add(instanceID);
					}
				}
			}
		}
	}

}
