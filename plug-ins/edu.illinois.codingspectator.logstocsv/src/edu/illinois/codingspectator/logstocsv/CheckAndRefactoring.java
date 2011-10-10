/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.logstocsv;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mohsen Vakilian
 * 
 */
public class CheckAndRefactoring implements Mappable {

	Event refactoring;

	Event check;

	public CheckAndRefactoring(Event refactoring, Event check) {
		this.refactoring= refactoring;
		this.check= check;
	}

	@Override
	public Map<String, String> toMap() {
		Map<String, String> map= new HashMap<String, String>();
		map.put("username", refactoring.username);
		map.put("workspace ID", refactoring.workspaceID);
		map.put("codingspectator version", refactoring.toMap().get("codingspectator version"));
		map.put("refactoring ID", refactoring.toMap().get("id"));
		map.put("refactoring timestamp", String.valueOf(refactoring.getTimestamp()));
		map.put("check timestamp", String.valueOf(check.getTimestamp()));
		return map;
	}

}
