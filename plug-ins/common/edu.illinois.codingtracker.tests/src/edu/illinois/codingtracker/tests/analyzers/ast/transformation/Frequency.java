/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.ast.transformation;

import java.util.Iterator;
import java.util.List;



/**
 * 
 * @author Stas Negara
 * 
 */
public class Frequency implements Comparable<Frequency> {

	private final int overallFrequency; //Is the sum of the minimal frequencies.

	private final List<Integer> minimalFrequencies;

	private final List<Integer> maximalFrequencies;


	public Frequency(List<Integer> minimalFrequencies, List<Integer> maximalFrequencies) {
		this.minimalFrequencies= minimalFrequencies;
		this.maximalFrequencies= maximalFrequencies;

		//TODO: Disable this check when not testing to speed up a little bit.
		if (!minimalFrequencies.isEmpty() && !minimalFrequencies.get(0).equals(maximalFrequencies.get(0))) {
			throw new RuntimeException("The first minimal and maximal frequencies differ!");
		}

		int accOverallFrequency= 0;
		for (int frequency : minimalFrequencies) {
			accOverallFrequency+= frequency;
		}
		overallFrequency= accOverallFrequency;
	}

	public int getMemorySize() {
		return (overallFrequency + minimalFrequencies.size() * 2) * 4;
	}

	public int getOverallFrequency() {
		return overallFrequency;
	}

	public boolean isEqualTo(Frequency other) {
		if (overallFrequency == other.overallFrequency && minimalFrequencies.equals(other.minimalFrequencies) &&
				maximalFrequencies.equals(other.maximalFrequencies)) {
			return true;
		}
		return false;
	}

	/**
	 * A frequency is not less powerful than the other frequency if it does not have any elements
	 * that are smaller than the corresponding elements in the other frequency.
	 * 
	 * @param other
	 * @return
	 */
	public boolean isNotLessPowerfulThan(Frequency other) {
		if (overallFrequency < other.overallFrequency) {
			return false;
		}
		Iterator<Integer> otherMinimalFrequenciesIterator= other.minimalFrequencies.iterator();
		for (int minFrequency : minimalFrequencies) {
			if (minFrequency < otherMinimalFrequenciesIterator.next()) {
				return false;
			}
		}
		Iterator<Integer> otherMaximalFrequenciesIterator= other.maximalFrequencies.iterator();
		for (int maxFrequency : maximalFrequencies) {
			if (maxFrequency < otherMaximalFrequenciesIterator.next()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Performs lexicographical comparison of two frequencies.
	 */
	@Override
	public int compareTo(Frequency other) {
		if (overallFrequency < other.overallFrequency) {
			return -1;
		}
		if (overallFrequency > other.overallFrequency) {
			return 1;
		}
		Iterator<Integer> otherMinimalFrequenciesIterator= other.minimalFrequencies.iterator();
		for (int minFrequency : minimalFrequencies) {
			Integer otherMinFrequency= otherMinimalFrequenciesIterator.next();
			if (minFrequency < otherMinFrequency) {
				return -1;
			}
			if (minFrequency > otherMinFrequency) {
				return 1;
			}
		}
		Iterator<Integer> otherMaximalFrequenciesIterator= other.maximalFrequencies.iterator();
		for (int maxFrequency : maximalFrequencies) {
			Integer otherMaxFrequency= otherMaximalFrequenciesIterator.next();
			if (maxFrequency < otherMaxFrequency) {
				return -1;
			}
			if (maxFrequency > otherMaxFrequency) {
				return 1;
			}
		}
		return 0;
	}

}
