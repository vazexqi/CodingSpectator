/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.ast.transformation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;


/**
 * 
 * @author Stas Negara
 * 
 */
public class UnknownTransformationMiner {

	private static Map<Integer, Transaction> transactions= new HashMap<Integer, Transaction>();

	private static TreeMap<String, Set<Integer>> inputTransactionItemsets= new TreeMap<String, Set<Integer>>();

	private static Map<String, Set<Integer>> resultTransactionItemsets= new TreeMap<String, Set<Integer>>();

	private static long elementID;


	public static void mine(String sequence, int maxTransformationSize) {
		resetState();
		char[] charSequence= sequence.toCharArray();
		int lastBlockNumber= (int)Math.ceil((double)charSequence.length / maxTransformationSize) - 1;
		for (int i= 0; i < charSequence.length; i++) {
			int blockNumber= i / maxTransformationSize;
			addElementToTransactions(charSequence[i], blockNumber, blockNumber < lastBlockNumber);
		}
		solve();
	}

	private static void solve() {
		resultTransactionItemsets.putAll(inputTransactionItemsets);
		for (String element : inputTransactionItemsets.keySet()) {
			solve(element, inputTransactionItemsets.tailMap(element, false));
		}
	}

	private static void solve(String itemSet, NavigableMap<String, Set<Integer>> tailMap) {
		Set<Integer> itemSetTransactions= resultTransactionItemsets.get(itemSet);
		for (Entry<String, Set<Integer>> entry : tailMap.entrySet()) {
			Set<Integer> commonTransactions= SetHelper.intersectTreeSets(itemSetTransactions, entry.getValue());
			if (commonTransactions.size() > 0) {
				String newItemSet= itemSet + entry.getKey();
				resultTransactionItemsets.put(newItemSet, commonTransactions);
				solve(newItemSet, inputTransactionItemsets.tailMap(entry.getKey(), false));
			}
		}
	}

	private static void addElementToTransactions(char element, int blockNumber, boolean addToConsequentTransaction) {
		String stringElement= String.valueOf(element);
		Set<Integer> elementTransactions= inputTransactionItemsets.get(stringElement);
		if (elementTransactions == null) {
			elementTransactions= new TreeSet<Integer>();
			inputTransactionItemsets.put(stringElement, elementTransactions);
		}
		if (blockNumber == 0) {
			//First block goes to the first transaction only
			elementTransactions.add(1);
			addElementInstanceToTransation(1, stringElement);
		} else {
			elementTransactions.add(blockNumber);
			addElementInstanceToTransation(blockNumber, stringElement);
			if (addToConsequentTransaction) {
				elementTransactions.add(blockNumber + 1);
				addElementInstanceToTransation(blockNumber + 1, stringElement);
			}
		}
		elementID++;
	}

	private static void addElementInstanceToTransation(int transactionID, String element) {
		Transaction transaction= transactions.get(transactionID);
		if (transaction == null) {
			transaction= new Transaction(transactionID);
			transactions.put(transactionID, transaction);
		}
		transaction.addItemInstance(element, elementID);
	}

	public static int getFrequency(String itemSet) {
		int frequency= 0;
		Set<Integer> transactionIDs= resultTransactionItemsets.get(itemSet);
		if (transactionIDs != null && transactionIDs.size() > 0) {
			Iterator<Integer> transactionIDIterator= transactionIDs.iterator();
			Transaction precedingTransaction= transactions.get(transactionIDIterator.next());
			while (transactionIDIterator.hasNext()) {
				Transaction subsequentTransaction= transactions.get(transactionIDIterator.next());
				precedingTransaction.removeDuplicatedInstances(subsequentTransaction, itemSet);
				frequency+= precedingTransaction.getFrequency(itemSet);
				precedingTransaction= subsequentTransaction;
			}
			frequency+= precedingTransaction.getFrequency(itemSet);
		}
		return frequency;
	}

	private static void resetState() {
		transactions.clear();
		inputTransactionItemsets.clear();
		resultTransactionItemsets.clear();
		elementID= 1;
	}

	public static void printState() {
		for (Entry<String, Set<Integer>> entry : resultTransactionItemsets.entrySet()) {
			String itemSet= entry.getKey();
			System.out.println("Frequency of item set \"" + itemSet + "\" is " + getFrequency(itemSet) + ":");
			for (int transactionNumber : entry.getValue()) {
				System.out.print("Instances for transaction " + transactionNumber + ": ");
				transactions.get(transactionNumber).printItemSetInstances(itemSet);
			}
		}
	}

}
