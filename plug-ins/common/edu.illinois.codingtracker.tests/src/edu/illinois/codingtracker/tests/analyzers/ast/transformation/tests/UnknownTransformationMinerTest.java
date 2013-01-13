/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.ast.transformation.tests;

import static org.junit.Assert.assertEquals;

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
	public void mineSmallPatterns1() {
		mine("abab", 1);
		assertEquals(2, getFrequency("a"));
		assertEquals(2, getFrequency("b"));
		assertEquals(2, getFrequency("ab"));

		mine("abba", 1);
		assertEquals(2, getFrequency("a"));
		assertEquals(2, getFrequency("b"));
		assertEquals(2, getFrequency("ab"));

		mine("aabb", 1);
		assertEquals(2, getFrequency("a"));
		assertEquals(2, getFrequency("b"));
		assertEquals(1, getFrequency("ab"));
	}

	@Test
	public void mineSmallPatterns2() {
		mine("abklabmn", 2);
		assertEquals(2, getFrequency("a"));
		assertEquals(2, getFrequency("b"));
		assertEquals(2, getFrequency("ab"));
		assertEquals(1, getFrequency("abkl"));
		assertEquals(1, getFrequency("abmn"));
		assertEquals(0, getFrequency("klmn"));

		mine("abklmnba", 2);
		assertEquals(2, getFrequency("a"));
		assertEquals(2, getFrequency("b"));
		assertEquals(2, getFrequency("ab"));
		assertEquals(1, getFrequency("abkl"));
		assertEquals(1, getFrequency("abmn"));
		assertEquals(1, getFrequency("klmn"));

		mine("ababklmn", 2);
		assertEquals(2, getFrequency("a"));
		assertEquals(2, getFrequency("b"));
		assertEquals(2, getFrequency("ab"));
		assertEquals(1, getFrequency("abkl"));
		assertEquals(0, getFrequency("abmn"));
		assertEquals(1, getFrequency("klmn"));
	}

	@Test
	public void mineMediumPatterns() {
		mine("dacbcaegfddacbcaegfdklmn", 6);
		assertEquals(4, getFrequency("a"));
		assertEquals(2, getFrequency("b"));
		assertEquals(4, getFrequency("c"));
		assertEquals(4, getFrequency("d"));
		assertEquals(2, getFrequency("e"));
		assertEquals(2, getFrequency("f"));
		assertEquals(2, getFrequency("g"));
		assertEquals(2, getFrequency("ab"));
		assertEquals(4, getFrequency("ac"));
		assertEquals(4, getFrequency("ad"));
		assertEquals(4, getFrequency("acd"));
		assertEquals(2, getFrequency("abc"));
		assertEquals(2, getFrequency("abd"));
		assertEquals(2, getFrequency("abcdefg"));
		assertEquals(2, getFrequency("acdefg"));
		assertEquals(2, getFrequency("abceg"));

		mine("dacbcaegfddacbcaegfdklmn", 3);
		assertEquals(4, getFrequency("a"));
		assertEquals(2, getFrequency("b"));
		assertEquals(4, getFrequency("c"));
		assertEquals(4, getFrequency("d"));
		assertEquals(2, getFrequency("e"));
		assertEquals(2, getFrequency("f"));
		assertEquals(2, getFrequency("g"));
		assertEquals(2, getFrequency("ab"));
		assertEquals(4, getFrequency("ac"));
		assertEquals(3, getFrequency("ad"));
		assertEquals(2, getFrequency("acd"));
		assertEquals(2, getFrequency("abc"));
		assertEquals(2, getFrequency("abd"));
		assertEquals(0, getFrequency("abcdefg"));
		assertEquals(0, getFrequency("acdefg"));
		assertEquals(2, getFrequency("abceg"));
	}

	@Test
	public void mineLargePatterns() {
		mine("dacbcaegfddacbcaegfdklmndacbcaegfddacbcaegfdklmn", 6);
		assertEquals(8, getFrequency("a"));
		assertEquals(4, getFrequency("b"));
		assertEquals(8, getFrequency("c"));
		assertEquals(8, getFrequency("d"));
		assertEquals(4, getFrequency("e"));
		assertEquals(4, getFrequency("f"));
		assertEquals(4, getFrequency("g"));
		assertEquals(4, getFrequency("ab"));
		assertEquals(8, getFrequency("ac"));
		assertEquals(8, getFrequency("ad"));
		assertEquals(8, getFrequency("acd"));
		assertEquals(4, getFrequency("abc"));
		assertEquals(4, getFrequency("abd"));
		assertEquals(4, getFrequency("abcdefg"));
		assertEquals(4, getFrequency("acdefg"));
		assertEquals(4, getFrequency("abceg"));

		mine("dacbcaegfddacbcaegfdklmndacbcaegfddacbcaegfdklmn", 3);
		assertEquals(8, getFrequency("a"));
		assertEquals(4, getFrequency("b"));
		assertEquals(8, getFrequency("c"));
		assertEquals(8, getFrequency("d"));
		assertEquals(4, getFrequency("e"));
		assertEquals(4, getFrequency("f"));
		assertEquals(4, getFrequency("g"));
		assertEquals(4, getFrequency("ab"));
		assertEquals(8, getFrequency("ac"));
		assertEquals(6, getFrequency("ad"));
		assertEquals(4, getFrequency("acd"));
		assertEquals(4, getFrequency("abc"));
		assertEquals(4, getFrequency("abd"));
		assertEquals(0, getFrequency("abcdefg"));
		assertEquals(0, getFrequency("acdefg"));
		assertEquals(4, getFrequency("abceg"));

		mine("dacbcaegfddacbcaegfdklmndacbcaegfddacbcaegfdklmndacbcaegfddacbcaegfdklmndacbcaegfddacbcaegfdklmn", 6);
		assertEquals(16, getFrequency("a"));
		assertEquals(8, getFrequency("b"));
		assertEquals(16, getFrequency("c"));
		assertEquals(16, getFrequency("d"));
		assertEquals(8, getFrequency("e"));
		assertEquals(8, getFrequency("f"));
		assertEquals(8, getFrequency("g"));
		assertEquals(8, getFrequency("ab"));
		assertEquals(16, getFrequency("ac"));
		assertEquals(16, getFrequency("ad"));
		assertEquals(16, getFrequency("acd"));
		assertEquals(8, getFrequency("abc"));
		assertEquals(8, getFrequency("abd"));
		assertEquals(8, getFrequency("abcdefg"));
		assertEquals(8, getFrequency("acdefg"));
		assertEquals(8, getFrequency("abceg"));

		mine("dacbcaegfddacbcaegfdklmndacbcaegfddacbcaegfdklmndacbcaegfddacbcaegfdklmndacbcaegfddacbcaegfdklmn", 3);
		assertEquals(16, getFrequency("a"));
		assertEquals(8, getFrequency("b"));
		assertEquals(16, getFrequency("c"));
		assertEquals(16, getFrequency("d"));
		assertEquals(8, getFrequency("e"));
		assertEquals(8, getFrequency("f"));
		assertEquals(8, getFrequency("g"));
		assertEquals(8, getFrequency("ab"));
		assertEquals(16, getFrequency("ac"));
		assertEquals(12, getFrequency("ad"));
		assertEquals(8, getFrequency("acd"));
		assertEquals(8, getFrequency("abc"));
		assertEquals(8, getFrequency("abd"));
		assertEquals(0, getFrequency("abcdefg"));
		assertEquals(0, getFrequency("acdefg"));
		assertEquals(8, getFrequency("abceg"));
	}

}
