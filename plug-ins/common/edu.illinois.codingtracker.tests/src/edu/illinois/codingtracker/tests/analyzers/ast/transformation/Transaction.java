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
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import edu.illinois.codingtracker.tests.analyzers.ast.transformation.helpers.SetMapHelper;


/**
 * 
 * @author Stas Negara
 * 
 */
public class Transaction {

	private final int transactionID;

	private Map<Item, Set<Long>> itemInstances= new HashMap<Item, Set<Long>>();

	//A temporary collection, which is used when a frequency of a particular itemset is computed.
	private Set<Long> removedDuplicatedInstanceIDs= new HashSet<Long>();


	public Transaction(int transactionID) {
		this.transactionID= transactionID;
	}

	public int getMemorySize() {
		int memorySize= transactionID * 4;
		for (Entry<Item, Set<Long>> entry : itemInstances.entrySet()) {
			memorySize+= (1 + entry.getValue().size()) * 8;
		}
		memorySize+= removedDuplicatedInstanceIDs.size() * 8;
		return memorySize;
	}

	public Set<Long> getAllItemInstances(Item item) {
		Set<Long> copySet= new HashSet<Long>();
		copySet.addAll(itemInstances.get(item));
		return copySet;
	}

	public void clearRemovedDuplicatedItemInstances() {
		removedDuplicatedInstanceIDs.clear();
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
		removedDuplicatedInstanceIDs.addAll(newRemovedDuplicatedItemInstances);
	}

	public StringBuffer getItemSetInstancesAsText(TreeSet<Item> itemSet) {
		StringBuffer result= new StringBuffer();
		for (Item item : itemSet) {
			for (long itemInstanceID : itemInstances.get(item)) {
				String marker= "";
				if (removedDuplicatedInstanceIDs.contains(itemInstanceID)) {
					marker= "~";
				}
				result.append(marker).append(itemInstanceID).append(marker).append(", ");
			}
			result.append("||");
		}
		result.append("\n");
		return result;
	}

	private List<Set<Long>> getValidItemSetInstances(TreeSet<Item> itemSet) {
		List<Set<Long>> validItemSetInstances= new LinkedList<Set<Long>>();
		for (Item item : itemSet) {
			Set<Long> validInstanceIDs= new TreeSet<Long>();
			for (long itemInstanceID : itemInstances.get(item)) {
				if (!removedDuplicatedInstanceIDs.contains(itemInstanceID)) {
					validInstanceIDs.add(itemInstanceID);
				}
			}
			validItemSetInstances.add(validInstanceIDs);
		}
		return validItemSetInstances;
	}

	private List<Set<Long>> getAllItemSetInstances(TreeSet<Item> itemSet) {
		List<Set<Long>> allItemSetInstances= new LinkedList<Set<Long>>();
		for (Item item : itemSet) {
			allItemSetInstances.add(itemInstances.get(item));
		}
		return allItemSetInstances;
	}

	public int getMinimalFrequency(TreeSet<Item> itemSet) {
		return SetMapHelper.getMinimumSetSize(getValidItemSetInstances(itemSet));
	}

	public int getMaximalFrequency(TreeSet<Item> itemSet) {
		return SetMapHelper.getMinimumSetSize(getAllItemSetInstances(itemSet));
	}

	public void removeDuplicatedInstances(Transaction subsequentTransaction, TreeSet<Item> itemSet) {
		if (transactionID >= subsequentTransaction.transactionID) {
			throw new RuntimeException("Should remove duplicates only versus the subsequent transaction!");
		}
		if (subsequentTransaction.transactionID - transactionID > 1) {
			return; //Duplicates can occur only in adjacent transactions.
		}

		Set<Long> toRemoveFromThis= new HashSet<Long>();
		Set<Long> toRemoveFromSubsequent= new HashSet<Long>();
		collectInstancesToRemove(getValidItemSetInstances(itemSet), subsequentTransaction.getValidItemSetInstances(itemSet), toRemoveFromThis, toRemoveFromSubsequent);

		subsequentTransaction.addRemovedDuplicatedItemInstances(itemSet, toRemoveFromSubsequent);
		if (SetMapHelper.getMinimumSetSize(subsequentTransaction.getValidItemSetInstances(itemSet)) == 0) {
			//If the subsequent transaction is destroyed, remove all duplicates from it.
			subsequentTransaction.addRemovedDuplicatedItemInstances(itemSet, toRemoveFromThis);
		} else {
			addRemovedDuplicatedItemInstances(itemSet, toRemoveFromThis);
		}
	}

	private void collectInstancesToRemove(List<Set<Long>> thisItemSetInstances, List<Set<Long>> subsequentItemSetInstances, Set<Long> toRemoveFromThis, Set<Long> toRemoveFromSubsequent) {
		int thisMinimumSetSize= SetMapHelper.getMinimumSetSize(thisItemSetInstances);
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
