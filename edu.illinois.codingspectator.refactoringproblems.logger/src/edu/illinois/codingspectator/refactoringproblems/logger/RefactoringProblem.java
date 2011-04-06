/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.refactoringproblems.logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;

/**
 * 
 * @author Balaji Ambresh Rajkumar
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class RefactoringProblem implements Serializable {

	/**
	 * This should be regenerated each time the class has fields added/removed or any other change
	 * that modifies its serialization
	 */
	private static final long serialVersionUID= 109903743362844765L;

	private Map<String, String> persistableAttributes;

	private String resourcePath;

	private String projectName;


	public RefactoringProblem(IMarkerDelta delta) {
		persistableAttributes= createPersistableVersionOfMap(delta);
		initializeAffectedResourceMetaInfo(delta);
	}

	private Map<String, String> createPersistableVersionOfMap(IMarkerDelta delta) {
		@SuppressWarnings("rawtypes")
		Map unpersistableAttributes= delta.getAttributes();
		Map<String, String> persistableAttributes= new HashMap<String, String>();

		for (Object key : unpersistableAttributes.keySet()) {
			// There is some domain knowledge about the values that can be stored in the IMarkerDelta#getAttributes
			Object object= unpersistableAttributes.get(key);
			if (object instanceof Boolean) {
				Boolean value= (Boolean)object;
				persistableAttributes.put((String)key, value.toString());
			} else if (object instanceof Integer) {
				Integer value= (Integer)object;
				persistableAttributes.put((String)key, value.toString());
			} else {
				String value= (String)object;
				persistableAttributes.put((String)key, value);
			}
		}
		return persistableAttributes;
	}

	private void initializeAffectedResourceMetaInfo(IMarkerDelta delta) {
		IResource resource= delta.getResource();
		resourcePath= resource.getFullPath().toString();
		projectName= resource.getProject().toString();
	}

	//////////////////////////////
	// This part is for XMLEncoder
	//////////////////////////////

	public RefactoringProblem() {

	}

	public Map<String, String> getPersistableAttributes() {
		return persistableAttributes;
	}

	public String getResourcePath() {
		return resourcePath;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setPersistableAttributes(Map<String, String> attributes) {
		this.persistableAttributes= attributes;
	}

	public void setResourcePath(String resourcePath) {
		this.resourcePath= resourcePath;
	}

	public void setProjectName(String projectName) {
		this.projectName= projectName;
	}

}
