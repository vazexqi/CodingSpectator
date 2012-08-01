/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation.RefactoringKind;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.CorrectiveRefactoringProperty;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.RefactoringProperty;


/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class InferredRefactoring {

	public static final long oldAgeTimeThreshold= 2 * 60 * 1000; // 2 minutes until a refactoring becomes too old.

	private final Map<String, List<RefactoringProperty>> properties= new HashMap<String, List<RefactoringProperty>>();

	private ASTOperation lastContributingOperation;

	protected abstract Set<String> getAcceptableProperties();

	public abstract RefactoringKind getKind();

	protected abstract InferredRefactoring createFreshInstance();

	protected abstract boolean isMultiProperty(String propertyName);

	/**
	 * Should be called only for complete refactorings.
	 * 
	 * @return
	 */
	public abstract Map<String, String> getArguments();

	protected static void addProperty(InferredRefactoring inferredRefactoring, RefactoringProperty refactoringProperty) {
		List<RefactoringProperty> propertiesList= getPropertiesList(inferredRefactoring, refactoringProperty.getClassName());
		propertiesList.add(refactoringProperty);
		refactoringProperty.addRefactoring(inferredRefactoring);
		inferredRefactoring.lastContributingOperation= refactoringProperty.getLastRelatedOperation();
	}

	private static List<RefactoringProperty> getPropertiesList(InferredRefactoring inferredRefactoring, String propertyName) {
		List<RefactoringProperty> propertiesList= inferredRefactoring.properties.get(propertyName);
		if (propertiesList == null) {
			propertiesList= new LinkedList<RefactoringProperty>();
			inferredRefactoring.properties.put(propertyName, propertiesList);
		}
		return propertiesList;
	}

	protected boolean isOptionalProperty(String propertyName) {
		return false; //By default all properties are required.
	}

	/**
	 * This method returns true iff the given propertyName is multi-property and this refactoring
	 * already contains at least one property of this kind.
	 * 
	 * @param propertyName
	 * @return
	 */
	public boolean hasMultiProperty(String propertyName) {
		if (!isMultiProperty(propertyName)) {
			return false;
		}
		List<RefactoringProperty> propertiesList= properties.get(propertyName);
		return propertiesList != null && !propertiesList.isEmpty();
	}

	public int getPropertiesCount() {
		return getAllProperties().size();
	}

	protected List<RefactoringProperty> getPropertiesList(String propertyName) {
		return properties.get(propertyName);
	}

	protected RefactoringProperty getProperty(String propertyName) {
		//It does not matter which property is returned if there are several of them in the corresponding list.
		return properties.get(propertyName).get(0);
	}

	public ASTOperation getLastContributingOperation() {
		return lastContributingOperation;
	}

	public void setLastContributingOperation(ASTOperation lastContributingOperation) {
		this.lastContributingOperation= lastContributingOperation;
	}

	public void setRefactoringID(long refactoringID) {
		for (RefactoringProperty refactoringProperty : getAllProperties()) {
			refactoringProperty.setRefactoringID(refactoringID);
		}
	}

	public boolean addPossiblyRelatedOperation(ASTOperation operation) {
		boolean wasAdded= false;
		for (RefactoringProperty refactoringProperty : getAllProperties()) {
			if (refactoringProperty.addPossiblyRelatedOperation(operation)) {
				wasAdded= true;
			}
		}
		return wasAdded;
	}

	public boolean isComplete() {
		for (String acceptableProperty : getAcceptableProperties()) {
			if (properties.get(acceptableProperty) == null && !isOptionalProperty(acceptableProperty)) {
				return false;
			}
		}
		return true;
	}

	public boolean isOld(long currentTimestamp) {
		if (currentTimestamp - lastContributingOperation.getTime() >= oldAgeTimeThreshold) {
			return true;
		}
		return false;
	}

	public boolean canBePart(RefactoringProperty refactoringProperty) {
		String propertyName= refactoringProperty.getClassName();
		if (refactoringProperty instanceof CorrectiveRefactoringProperty ||
				!getAcceptableProperties().contains(propertyName) ||
				properties.get(propertyName) != null && !isMultiProperty(propertyName)) {
			return false;
		}
		for (RefactoringProperty property : getAllProperties()) {
			if (!property.doesMatch(this, refactoringProperty)) {
				return false;
			}
		}
		return true;
	}

	public boolean doesAffectSameEntity(RefactoringProperty refactoringProperty) {
		for (RefactoringProperty property : getAllProperties()) {
			if (property.doesAffectSameEntity(refactoringProperty)) {
				return true;
			}
		}
		return false;
	}

	public boolean doesAffectSameEntity(InferredRefactoring refactoring) {
		for (RefactoringProperty property : refactoring.getAllProperties()) {
			if (doesAffectSameEntity(property)) {
				return true;
			}
		}
		return false;
	}

	protected Set<RefactoringProperty> getAllProperties() {
		Set<RefactoringProperty> allProperties= new HashSet<RefactoringProperty>();
		for (List<RefactoringProperty> propertiesList : properties.values()) {
			for (RefactoringProperty property : propertiesList) {
				allProperties.add(property);
			}
		}
		return allProperties;
	}

	/**
	 * The argument 'addToCopy' indicates whether the property should be added to a new refactoring
	 * obtained by copying this refactoring (if true) or should be added directly to this
	 * refactoring (if false), which is legal only if this refactoring is complete.
	 * 
	 * @param refactoringProperty
	 */
	public InferredRefactoring addProperty(RefactoringProperty refactoringProperty, boolean addToCopy) {
		if (!canBePart(refactoringProperty)) {
			throw new RuntimeException("Can not add property: " + refactoringProperty);
		}
		if (addToCopy) {
			InferredRefactoring resultRefactoring= createCopy();
			addProperty(resultRefactoring, refactoringProperty);
			return resultRefactoring;
		}
		if (!isComplete() && !isMultiProperty(refactoringProperty.getClassName())) {
			throw new RuntimeException("Can not add a non-multiproperty to a non-complete refactoring!");
		}
		addProperty(this, refactoringProperty);
		return this;
	}

	private InferredRefactoring createCopy() {
		InferredRefactoring copyRefactoring= createFreshInstance();
		for (Entry<String, List<RefactoringProperty>> entry : properties.entrySet()) {
			List<RefactoringProperty> copyList= new LinkedList<RefactoringProperty>();
			copyList.addAll(entry.getValue());
			copyRefactoring.properties.put(entry.getKey(), copyList);
		}
		for (RefactoringProperty property : getAllProperties()) {
			property.addRefactoring(copyRefactoring);
		}
		return copyRefactoring;
	}

	public void disableProperties() {
		//Use a temporary collection since disabling properties removes them from the main collection.
		Set<RefactoringProperty> existingProperties= getAllProperties();
		for (RefactoringProperty property : existingProperties) {
			property.disable();
		}
	}

	public void disabledProperty(RefactoringProperty disabledProperty) {
		//Complete refactorings are unaffected by disabled properties.
		if (!isComplete()) {
			String disabledPropertyName= disabledProperty.getClassName();
			List<RefactoringProperty> propertiesList= properties.get(disabledPropertyName);
			//TODO: Why it could be null (i.e., why properties could be empty at this point like in cs-509 sequence)?
			if (propertiesList != null) {
				propertiesList.remove(disabledProperty);
				if (propertiesList.size() == 0) {
					properties.remove(disabledPropertyName);
				}
			}
			if (properties.size() == 0) {
				InferredRefactoringFactory.destroyedRefactoring(this);
			}
		}
	}

	public void correctedProperty(RefactoringProperty correctedProperty) {
		for (RefactoringProperty existingProperty : getAllProperties()) {
			if (!existingProperty.doesMatch(this, correctedProperty)) {
				//Refactoring is destroyed due to the correction.
				for (RefactoringProperty property : getAllProperties()) {
					property.removeRefactoring(this);
				}
				properties.clear();
				InferredRefactoringFactory.destroyedRefactoring(this);
				return;
			}
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InferredRefactoring other= (InferredRefactoring)obj;
		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;
		return true;
	}

}
