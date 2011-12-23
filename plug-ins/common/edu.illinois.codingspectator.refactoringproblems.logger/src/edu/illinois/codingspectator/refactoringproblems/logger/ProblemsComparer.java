/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.refactoringproblems.logger;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author Balaji Ambresh Rajkumar
 * @author Mohsen Vakilian
 * @author nchen
 * @author Stas Negara
 * 
 */
public class ProblemsComparer {

	private long refactoringTimestamp= -1;

	private long previousTimestamp= -1;

	private Set<DefaultProblemWrapper> previousProblems;

	private long currentTimestamp= -1;

	private Set<DefaultProblemWrapper> currentProblems;

	public void setRefactoringTimestamp(long refactoringTimestamp) {
		this.refactoringTimestamp= refactoringTimestamp;
	}

	public void pushNewProblemsSet(Set<DefaultProblemWrapper> problems) {
		previousProblems= currentProblems;
		previousTimestamp= currentTimestamp;
		currentProblems= problems;
		currentTimestamp= System.currentTimeMillis();
	}

	public ProblemChanges compareProblems() {
		return new ProblemChanges(refactoringTimestamp, currentTimestamp, setDifference(currentProblems, previousProblems), previousTimestamp, setDifference(previousProblems, currentProblems));
	}

	/**
	 * 
	 * @param left
	 * @param right
	 * @return left - right
	 */
	private Set<DefaultProblemWrapper> setDifference(Set<DefaultProblemWrapper> left, Set<DefaultProblemWrapper> right) {
		Set<DefaultProblemWrapper> copyOfLeft= new HashSet<DefaultProblemWrapper>(left);
		copyOfLeft.removeAll(right);
		return copyOfLeft;
	}

}
