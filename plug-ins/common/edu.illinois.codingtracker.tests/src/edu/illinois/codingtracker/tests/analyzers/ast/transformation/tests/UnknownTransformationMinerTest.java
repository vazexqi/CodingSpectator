/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.ast.transformation.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import edu.illinois.codingtracker.tests.analyzers.ast.transformation.UnknownTransformationMiner;



/**
 * 
 * @author Stas Negara
 * 
 */
public class UnknownTransformationMinerTest {

	@Test
	public void mineSmallPatterns1() {
		UnknownTransformationMiner.mine("abab", 1);
		assertEquals(2, UnknownTransformationMiner.getFrequency("a"));
		assertEquals(2, UnknownTransformationMiner.getFrequency("b"));
		assertEquals(2, UnknownTransformationMiner.getFrequency("ab"));

		UnknownTransformationMiner.mine("abba", 1);
		assertEquals(2, UnknownTransformationMiner.getFrequency("a"));
		assertEquals(2, UnknownTransformationMiner.getFrequency("b"));
		assertEquals(2, UnknownTransformationMiner.getFrequency("ab"));

		UnknownTransformationMiner.mine("aabb", 1);
		assertEquals(2, UnknownTransformationMiner.getFrequency("a"));
		assertEquals(2, UnknownTransformationMiner.getFrequency("b"));
		assertEquals(1, UnknownTransformationMiner.getFrequency("ab"));
	}

	@Test
	public void mineSmallPatterns2() {
		UnknownTransformationMiner.mine("abklabmn", 2);
		assertEquals(2, UnknownTransformationMiner.getFrequency("a"));
		assertEquals(2, UnknownTransformationMiner.getFrequency("b"));
		assertEquals(2, UnknownTransformationMiner.getFrequency("ab"));
		assertEquals(1, UnknownTransformationMiner.getFrequency("abkl"));
		assertEquals(1, UnknownTransformationMiner.getFrequency("abmn"));
		assertEquals(0, UnknownTransformationMiner.getFrequency("klmn"));

		UnknownTransformationMiner.mine("abklmnba", 2);
		assertEquals(2, UnknownTransformationMiner.getFrequency("a"));
		assertEquals(2, UnknownTransformationMiner.getFrequency("b"));
		assertEquals(2, UnknownTransformationMiner.getFrequency("ab"));
		assertEquals(1, UnknownTransformationMiner.getFrequency("abkl"));
		assertEquals(1, UnknownTransformationMiner.getFrequency("abmn"));
		assertEquals(1, UnknownTransformationMiner.getFrequency("klmn"));

		UnknownTransformationMiner.mine("ababklmn", 2);
		assertEquals(2, UnknownTransformationMiner.getFrequency("a"));
		assertEquals(2, UnknownTransformationMiner.getFrequency("b"));
		assertEquals(2, UnknownTransformationMiner.getFrequency("ab"));
		assertEquals(1, UnknownTransformationMiner.getFrequency("abkl"));
		assertEquals(0, UnknownTransformationMiner.getFrequency("abmn"));
		assertEquals(1, UnknownTransformationMiner.getFrequency("klmn"));
	}

	@Test
	public void mineMediumPatterns() {
		UnknownTransformationMiner.mine("dacbcaegfddacbcaegfdklmn", 6);
		assertEquals(4, UnknownTransformationMiner.getFrequency("a"));
		assertEquals(2, UnknownTransformationMiner.getFrequency("b"));
		assertEquals(4, UnknownTransformationMiner.getFrequency("c"));
		assertEquals(4, UnknownTransformationMiner.getFrequency("d"));
		assertEquals(2, UnknownTransformationMiner.getFrequency("e"));
		assertEquals(2, UnknownTransformationMiner.getFrequency("f"));
		assertEquals(2, UnknownTransformationMiner.getFrequency("g"));
		assertEquals(2, UnknownTransformationMiner.getFrequency("ab"));
		assertEquals(4, UnknownTransformationMiner.getFrequency("ac"));
		assertEquals(4, UnknownTransformationMiner.getFrequency("ad"));
		assertEquals(4, UnknownTransformationMiner.getFrequency("acd"));
		assertEquals(2, UnknownTransformationMiner.getFrequency("abc"));
		assertEquals(2, UnknownTransformationMiner.getFrequency("abd"));
		assertEquals(2, UnknownTransformationMiner.getFrequency("abcdefg"));
		assertEquals(2, UnknownTransformationMiner.getFrequency("acdefg"));
		assertEquals(2, UnknownTransformationMiner.getFrequency("abceg"));

		UnknownTransformationMiner.mine("dacbcaegfddacbcaegfdklmn", 3);
		assertEquals(4, UnknownTransformationMiner.getFrequency("a"));
		assertEquals(2, UnknownTransformationMiner.getFrequency("b"));
		assertEquals(4, UnknownTransformationMiner.getFrequency("c"));
		assertEquals(4, UnknownTransformationMiner.getFrequency("d"));
		assertEquals(2, UnknownTransformationMiner.getFrequency("e"));
		assertEquals(2, UnknownTransformationMiner.getFrequency("f"));
		assertEquals(2, UnknownTransformationMiner.getFrequency("g"));
		assertEquals(2, UnknownTransformationMiner.getFrequency("ab"));
		assertEquals(4, UnknownTransformationMiner.getFrequency("ac"));
		assertEquals(3, UnknownTransformationMiner.getFrequency("ad"));
		assertEquals(2, UnknownTransformationMiner.getFrequency("acd"));
		assertEquals(2, UnknownTransformationMiner.getFrequency("abc"));
		assertEquals(2, UnknownTransformationMiner.getFrequency("abd"));
		assertEquals(0, UnknownTransformationMiner.getFrequency("abcdefg"));
		assertEquals(0, UnknownTransformationMiner.getFrequency("acdefg"));
		assertEquals(2, UnknownTransformationMiner.getFrequency("abceg"));
	}

	@Test
	public void mineLargePatterns() {
		UnknownTransformationMiner.mine("dacbcaegfddacbcaegfdklmndacbcaegfddacbcaegfdklmn", 6);
		assertEquals(8, UnknownTransformationMiner.getFrequency("a"));
		assertEquals(4, UnknownTransformationMiner.getFrequency("b"));
		assertEquals(8, UnknownTransformationMiner.getFrequency("c"));
		assertEquals(8, UnknownTransformationMiner.getFrequency("d"));
		assertEquals(4, UnknownTransformationMiner.getFrequency("e"));
		assertEquals(4, UnknownTransformationMiner.getFrequency("f"));
		assertEquals(4, UnknownTransformationMiner.getFrequency("g"));
		assertEquals(4, UnknownTransformationMiner.getFrequency("ab"));
		assertEquals(8, UnknownTransformationMiner.getFrequency("ac"));
		assertEquals(8, UnknownTransformationMiner.getFrequency("ad"));
		assertEquals(8, UnknownTransformationMiner.getFrequency("acd"));
		assertEquals(4, UnknownTransformationMiner.getFrequency("abc"));
		assertEquals(4, UnknownTransformationMiner.getFrequency("abd"));
		assertEquals(4, UnknownTransformationMiner.getFrequency("abcdefg"));
		assertEquals(4, UnknownTransformationMiner.getFrequency("acdefg"));
		assertEquals(4, UnknownTransformationMiner.getFrequency("abceg"));

		UnknownTransformationMiner.mine("dacbcaegfddacbcaegfdklmndacbcaegfddacbcaegfdklmn", 3);
		assertEquals(8, UnknownTransformationMiner.getFrequency("a"));
		assertEquals(4, UnknownTransformationMiner.getFrequency("b"));
		assertEquals(8, UnknownTransformationMiner.getFrequency("c"));
		assertEquals(8, UnknownTransformationMiner.getFrequency("d"));
		assertEquals(4, UnknownTransformationMiner.getFrequency("e"));
		assertEquals(4, UnknownTransformationMiner.getFrequency("f"));
		assertEquals(4, UnknownTransformationMiner.getFrequency("g"));
		assertEquals(4, UnknownTransformationMiner.getFrequency("ab"));
		assertEquals(8, UnknownTransformationMiner.getFrequency("ac"));
		assertEquals(6, UnknownTransformationMiner.getFrequency("ad"));
		assertEquals(4, UnknownTransformationMiner.getFrequency("acd"));
		assertEquals(4, UnknownTransformationMiner.getFrequency("abc"));
		assertEquals(4, UnknownTransformationMiner.getFrequency("abd"));
		assertEquals(0, UnknownTransformationMiner.getFrequency("abcdefg"));
		assertEquals(0, UnknownTransformationMiner.getFrequency("acdefg"));
		assertEquals(4, UnknownTransformationMiner.getFrequency("abceg"));

		UnknownTransformationMiner.mine("dacbcaegfddacbcaegfdklmndacbcaegfddacbcaegfdklmndacbcaegfddacbcaegfdklmndacbcaegfddacbcaegfdklmn", 6);
		assertEquals(16, UnknownTransformationMiner.getFrequency("a"));
		assertEquals(8, UnknownTransformationMiner.getFrequency("b"));
		assertEquals(16, UnknownTransformationMiner.getFrequency("c"));
		assertEquals(16, UnknownTransformationMiner.getFrequency("d"));
		assertEquals(8, UnknownTransformationMiner.getFrequency("e"));
		assertEquals(8, UnknownTransformationMiner.getFrequency("f"));
		assertEquals(8, UnknownTransformationMiner.getFrequency("g"));
		assertEquals(8, UnknownTransformationMiner.getFrequency("ab"));
		assertEquals(16, UnknownTransformationMiner.getFrequency("ac"));
		assertEquals(16, UnknownTransformationMiner.getFrequency("ad"));
		assertEquals(16, UnknownTransformationMiner.getFrequency("acd"));
		assertEquals(8, UnknownTransformationMiner.getFrequency("abc"));
		assertEquals(8, UnknownTransformationMiner.getFrequency("abd"));
		assertEquals(8, UnknownTransformationMiner.getFrequency("abcdefg"));
		assertEquals(8, UnknownTransformationMiner.getFrequency("acdefg"));
		assertEquals(8, UnknownTransformationMiner.getFrequency("abceg"));

		UnknownTransformationMiner.mine("dacbcaegfddacbcaegfdklmndacbcaegfddacbcaegfdklmndacbcaegfddacbcaegfdklmndacbcaegfddacbcaegfdklmn", 3);
		assertEquals(16, UnknownTransformationMiner.getFrequency("a"));
		assertEquals(8, UnknownTransformationMiner.getFrequency("b"));
		assertEquals(16, UnknownTransformationMiner.getFrequency("c"));
		assertEquals(16, UnknownTransformationMiner.getFrequency("d"));
		assertEquals(8, UnknownTransformationMiner.getFrequency("e"));
		assertEquals(8, UnknownTransformationMiner.getFrequency("f"));
		assertEquals(8, UnknownTransformationMiner.getFrequency("g"));
		assertEquals(8, UnknownTransformationMiner.getFrequency("ab"));
		assertEquals(16, UnknownTransformationMiner.getFrequency("ac"));
		assertEquals(12, UnknownTransformationMiner.getFrequency("ad"));
		assertEquals(8, UnknownTransformationMiner.getFrequency("acd"));
		assertEquals(8, UnknownTransformationMiner.getFrequency("abc"));
		assertEquals(8, UnknownTransformationMiner.getFrequency("abd"));
		assertEquals(0, UnknownTransformationMiner.getFrequency("abcdefg"));
		assertEquals(0, UnknownTransformationMiner.getFrequency("acdefg"));
		assertEquals(8, UnknownTransformationMiner.getFrequency("abceg"));
	}

}
