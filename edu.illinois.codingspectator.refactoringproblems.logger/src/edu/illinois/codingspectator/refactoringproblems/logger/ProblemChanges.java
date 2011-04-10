/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.refactoringproblems.logger;

import java.util.Set;

/**
 * This class contains the two way differences between the set of compilation problems before and
 * after a refactoring.
 * 
 * @author Mohsen Vakilian
 * @author Balaji Ambresh Rajkumar
 * 
 */
public class ProblemChanges {

	Set<DefaultProblemWrapper> afterMinusBefore;

	Set<DefaultProblemWrapper> beforeMinusAfter;

	public ProblemChanges(Set<DefaultProblemWrapper> afterMinusBefore, Set<DefaultProblemWrapper> beforeMinusAfter) {
		this.afterMinusBefore= afterMinusBefore;
		this.beforeMinusAfter= beforeMinusAfter;
	}

	@Override
	public String toString() {
		return "ProblemChanges [afterMinusBefore=" + afterMinusBefore + ", beforeMinusAfter=" + beforeMinusAfter + "]";
	}

}
