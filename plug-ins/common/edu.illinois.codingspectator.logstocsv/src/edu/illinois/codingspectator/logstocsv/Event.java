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

	protected static final int ATTRIBUTE_LENGTH_LIMIT= 100000; // Limit long strings to a fixed size when mapping to VARCHAR in SQL

	protected String username;

	protected String workspaceID;

	protected String codingspectatorVersion;

	public Event(String username, String workspaceID, String codingSpectatorVersion) {
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
