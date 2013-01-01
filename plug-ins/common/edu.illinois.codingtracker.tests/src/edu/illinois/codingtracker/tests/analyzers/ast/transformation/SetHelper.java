/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.ast.transformation;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


/**
 * 
 * @author Stas Negara
 * 
 */
public class SetHelper {

	/**
	 * The input parameters are supposed to be TreeSets!
	 * 
	 * @param treeSet1
	 * @param treeSet2
	 * @return
	 */
	public static Set<Integer> intersectTreeSets(Set<Integer> treeSet1, Set<Integer> treeSet2) {
		Set<Integer> result= new TreeSet<Integer>();
		result.addAll(treeSet1);
		result.retainAll(treeSet2);
		return result;
	}

	public static Set<Long> getAllInstancesAsSet(List<Set<Long>> itemSetInstances) {
		Set<Long> allInstances= new HashSet<Long>();
		for (Set<Long> instances : itemSetInstances) {
			allInstances.addAll(instances);
		}
		return allInstances;
	}

	public static int getMinimumSetSize(List<Set<Long>> itemSetInstances) {
		int minimumSetSize= Integer.MAX_VALUE;
		for (Set<Long> instances : itemSetInstances) {
			if (minimumSetSize > instances.size()) {
				minimumSetSize= instances.size();
			}
		}
		return minimumSetSize;
	}

	public static List<Set<Long>> subtractItemSetInstances(List<Set<Long>> itemSetInstances, List<Set<Long>> subtractedItemSetInstances) {
		List<Set<Long>> subtractionResult= new LinkedList<Set<Long>>();
		Iterator<Set<Long>> subtractedItemSetInstancesIterator= subtractedItemSetInstances.iterator();
		for (Set<Long> instances : itemSetInstances) {
			Set<Long> subtractionSet= new TreeSet<Long>();
			subtractionSet.addAll(instances);
			subtractionSet.removeAll(subtractedItemSetInstancesIterator.next());
			subtractionResult.add(subtractionSet);
		}
		return subtractionResult;
	}

}
