package edu.illinois.codingtracker.codeskimmer;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import edu.illinois.codingtracker.operations.UserOperation;

/**
 * 
 * @author Connor Simmons
 *
 */
public abstract class Filter {

	public abstract void init(List<UserOperation> operations);

	public abstract boolean matchesOperation(UserOperation operation);

	/*
	 * Checks that at least 1 of every filter type in the given list is
	 * satisfied. It's an || within a class but a && between classes.
	 */
	public static boolean checkFiltersForMatch(List<Filter> filters,
			UserOperation operation) {
		// gather all different interest class types
		ArrayList<String> filterClasses = new ArrayList<String>();

		for (Filter sel : filters) {
			if (!filters.contains(sel.getClass().getName())) {
				filterClasses.add(sel.getClass().getName());
			}
		}

		// now check each until each class has been satisfied
		Hashtable<String, Boolean> satisfiedClasses = new Hashtable<String, Boolean>();
		for (Filter sel : filters) {
			// skip if we already have this class satisfied
			if (satisfiedClasses.get(sel.getClass().getName()) != null
					&& satisfiedClasses.get(sel.getClass().getName())) {
				continue;
			}
			satisfiedClasses.put(sel.getClass().getName(),
					sel.matchesOperation(operation));
		}

		// return false if any of the class types was not satisfied
		boolean allSatisfied = true;
		for (String className : filterClasses) {
			if (satisfiedClasses.get(className) != null) {
				allSatisfied = allSatisfied && satisfiedClasses.get(className);
			}
		}

		return allSatisfied && filterClasses.size() > 0;
	}
	
	public static ArrayList<UserOperation> filterOperations(List<Filter> filters, List<UserOperation> operations) {
		ArrayList<UserOperation> filteredOps = new ArrayList<UserOperation>();
		for (UserOperation op : operations) {
			if (checkFiltersForMatch(filters, op)) {
				filteredOps.add(op);
			}
		}
		return filteredOps;
	}

}
