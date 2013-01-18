/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.ast.transformation.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.Test;

import edu.illinois.codingtracker.tests.analyzers.ast.transformation.Item;
import edu.illinois.codingtracker.tests.analyzers.ast.transformation.UnknownTransformationMiner;



/**
 * 
 * @author Stas Negara
 * 
 */
public class UnknownTransformationMinerTest {


	public void mine(String sequence, int maxTransformationSize) {
		checkMiningPreconditions(sequence, maxTransformationSize);
		UnknownTransformationMiner.resetState();
		long itemID= 1;
		boolean isFirstBlock= true;
		TreeMap<Long, Item> currentBlockItems= new TreeMap<Long, Item>();
		for (char c : sequence.toCharArray()) {
			if (currentBlockItems.size() == maxTransformationSize) {
				UnknownTransformationMiner.addItemToTransactions(currentBlockItems, isFirstBlock, false);
				isFirstBlock= false;
				currentBlockItems.clear();
			}
			currentBlockItems.put(itemID, new CharItem(c));
			itemID++;
		}
		UnknownTransformationMiner.addItemToTransactions(currentBlockItems, isFirstBlock, true);
		UnknownTransformationMiner.mine();
	}

	private void checkMiningPreconditions(String sequence, int maxTransformationSize) {
		if (sequence.isEmpty()) {
			throw new RuntimeException("Can not mine an empty sequence!");
		}
		if (maxTransformationSize < 1) {
			throw new RuntimeException("Maximum transformation size should be 1 or greater!");
		}
	}

	public int getFrequency(String sequence) {
		TreeSet<Item> itemSet= new TreeSet<Item>();
		for (char c : sequence.toCharArray()) {
			itemSet.add(new CharItem(c));
		}
		return UnknownTransformationMiner.getFrequency(itemSet);
	}

	@Test
	public void checkScalability() {
		long startTime= System.currentTimeMillis();

		mine("abcdefghijklmnopqrs", 100); //takes around 68.5 seconds for the unoptimized implementation.

		//This runs "forever" for the unoptimized implementation.
		mine("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890", 100);

		long miningTime= System.currentTimeMillis() - startTime;
		assertTrue(miningTime < 100);
	}

	@Test
	public void mineSmallPatterns1() {
		mine("abab", 1);
		assertEquals(0, getFrequency("a")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("b")); //Subsumed because it is not a closed itemset.
		assertEquals(2, getFrequency("ab"));

		mine("abba", 1);
		assertEquals(0, getFrequency("a")); //Subsumed because it is not a closed itemset.
		assertEquals(2, getFrequency("b")); //It is not subsumed because it is present in different transactions than [a, b].
		assertEquals(2, getFrequency("ab"));

		mine("aabb", 1);
		assertEquals(2, getFrequency("a")); //It is not subsumed because it is present in different transactions than [a, b] and has higher frequency.
		assertEquals(2, getFrequency("b")); //It is not subsumed because it is present in different transactions than [a, b] and has higher frequency.
		assertEquals(1, getFrequency("ab"));
	}

	@Test
	public void mineSmallPatterns2() {
		mine("abklabmn", 2);
		assertEquals(0, getFrequency("a")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("b")); //Subsumed because it is not a closed itemset.
		assertEquals(2, getFrequency("ab"));
		assertEquals(0, getFrequency("kl")); //Subsumed across branches.
		assertEquals(1, getFrequency("abkl"));
		assertEquals(0, getFrequency("mn")); //Subsumed across branches.
		assertEquals(1, getFrequency("abmn"));
		assertEquals(0, getFrequency("klmn"));

		mine("abklmnba", 2);
		assertEquals(0, getFrequency("a")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("b")); //Subsumed because it is not a closed itemset.
		assertEquals(2, getFrequency("ab"));
		assertEquals(1, getFrequency("kl")); //It is not subsumed because it is present in different transactions than [a, b, k, l].
		assertEquals(1, getFrequency("abkl"));
		assertEquals(1, getFrequency("mn")); //It is not subsumed because it is present in different transactions than [a, b, m, n].
		assertEquals(1, getFrequency("abmn"));
		assertEquals(1, getFrequency("klmn"));

		mine("ababklmn", 2);
		assertEquals(0, getFrequency("a")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("b")); //Subsumed because it is not a closed itemset.
		assertEquals(2, getFrequency("ab"));
		assertEquals(1, getFrequency("kl")); //It is not subsumed because it is present in different transactions than [a, b, k, l].
		assertEquals(1, getFrequency("abkl"));
		assertEquals(0, getFrequency("abmn"));
		assertEquals(1, getFrequency("klmn"));
	}

	@Test
	public void mineMediumPatterns() {
		mine("dacbcaegfddacbcaegfdklmn", 6);
		assertEquals(0, getFrequency("a")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("b")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("c")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("d")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("e")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("f")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("g")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("ab")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("ac")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("ad")); //Subsumed because it is not a closed itemset.
		assertEquals(4, getFrequency("acd"));
		assertEquals(0, getFrequency("abc")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("abd")); //Subsumed because it is not a closed itemset.
		assertEquals(2, getFrequency("abcdefg"));
		assertEquals(0, getFrequency("acdefg")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("abceg")); //Subsumed because it is not a closed itemset.

		mine("dacbcaegfddacbcaegfdklmn", 3);
		//Some of the following itemsets are not subsumed because having the same frequency as a superset, 
		//they are present in more transactions.
		assertEquals(4, getFrequency("a"));
		assertEquals(0, getFrequency("b")); //Subsumed across branches.
		assertEquals(0, getFrequency("c")); //Subsumed across branches.
		assertEquals(4, getFrequency("d"));
		assertEquals(0, getFrequency("e")); //Subsumed because it is not a closed itemset.
		assertEquals(2, getFrequency("f"));
		assertEquals(0, getFrequency("g")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("ab")); //Subsumed because it is not a closed itemset.
		assertEquals(4, getFrequency("ac"));
		assertEquals(3, getFrequency("ad"));
		assertEquals(2, getFrequency("abcd"));
		assertEquals(2, getFrequency("abc"));
		assertEquals(0, getFrequency("abd")); //Subsumed across branches.
		assertEquals(0, getFrequency("abcdefg"));
		assertEquals(0, getFrequency("acdefg"));
		assertEquals(2, getFrequency("abceg"));
	}

	@Test
	public void mineLargePatterns() {
		mine("dacbcaegfddacbcaegfdklmndacbcaegfddacbcaegfdklmn", 6);
		assertEquals(0, getFrequency("a")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("b")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("c")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("d")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("e")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("f")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("g")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("ab")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("ac")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("ad")); //Subsumed because it is not a closed itemset.
		assertEquals(8, getFrequency("acd"));
		assertEquals(0, getFrequency("abc")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("abd")); //Subsumed because it is not a closed itemset.
		assertEquals(4, getFrequency("abcdefg"));
		assertEquals(0, getFrequency("acdefg")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("abceg")); //Subsumed because it is not a closed itemset.

		mine("dacbcaegfddacbcaegfdklmndacbcaegfddacbcaegfdklmn", 3);
		assertEquals(8, getFrequency("a"));
		assertEquals(0, getFrequency("b")); //Subsumed across branches.
		assertEquals(0, getFrequency("c")); //Subsumed across branches.
		assertEquals(8, getFrequency("d"));
		assertEquals(0, getFrequency("e")); //Subsumed because it is not a closed itemset.
		assertEquals(4, getFrequency("f"));
		assertEquals(0, getFrequency("g")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("ab")); //Subsumed because it is not a closed itemset.
		assertEquals(8, getFrequency("ac"));
		assertEquals(6, getFrequency("ad"));
		assertEquals(4, getFrequency("abcd"));
		assertEquals(4, getFrequency("abc"));
		assertEquals(0, getFrequency("abd")); //Subsumed across branches.
		assertEquals(0, getFrequency("abcdefg"));
		assertEquals(0, getFrequency("acdefg"));
		assertEquals(4, getFrequency("abceg"));

		mine("dacbcaegfddacbcaegfdklmndacbcaegfddacbcaegfdklmndacbcaegfddacbcaegfdklmndacbcaegfddacbcaegfdklmn", 6);
		assertEquals(0, getFrequency("a")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("b")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("c")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("d")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("e")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("f")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("g")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("ab")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("ac")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("ad")); //Subsumed because it is not a closed itemset.
		assertEquals(16, getFrequency("acd"));
		assertEquals(0, getFrequency("abc")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("abd")); //Subsumed because it is not a closed itemset.
		assertEquals(8, getFrequency("abcdefg"));
		assertEquals(0, getFrequency("acdefg")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("abceg")); //Subsumed because it is not a closed itemset.

		mine("dacbcaegfddacbcaegfdklmndacbcaegfddacbcaegfdklmndacbcaegfddacbcaegfdklmndacbcaegfddacbcaegfdklmn", 3);
		assertEquals(16, getFrequency("a"));
		assertEquals(0, getFrequency("b")); //Subsumed across branches.
		assertEquals(0, getFrequency("c")); //Subsumed across branches.
		assertEquals(16, getFrequency("d"));
		assertEquals(0, getFrequency("e")); //Subsumed because it is not a closed itemset.
		assertEquals(8, getFrequency("f"));
		assertEquals(0, getFrequency("g")); //Subsumed because it is not a closed itemset.
		assertEquals(0, getFrequency("ab")); //Subsumed because it is not a closed itemset.
		assertEquals(16, getFrequency("ac"));
		assertEquals(12, getFrequency("ad"));
		assertEquals(8, getFrequency("abcd"));
		assertEquals(8, getFrequency("abc"));
		assertEquals(0, getFrequency("abd")); //Subsumed across branches.
		assertEquals(0, getFrequency("abcdefg"));
		assertEquals(0, getFrequency("acdefg"));
		assertEquals(8, getFrequency("abceg"));
	}

}
