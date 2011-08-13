/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.logstocsv;

import java.util.HashMap;
import java.util.Map;


/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public abstract class Event implements Mappable {

	protected String username;

	protected String workspaceID;

	protected String codingspectatorVersion;

	protected Event(String username, String workspaceID, String codingSpectatorVersion) {
		this.username= username;
		this.workspaceID= workspaceID;
		this.codingspectatorVersion= codingSpectatorVersion;
	}

	public Map<String, String> toMap() {
		Map<String, String> map= new HashMap<String, String>();
		map.put("username", username);
		map.put("workspace ID", workspaceID);
		map.put("codingspectator version", codingspectatorVersion);
		return map;
	}

	public abstract long getTimestamp();

}
