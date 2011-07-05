/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.helpers;

import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 * 
 * @author Stas Negara
 * 
 */
public class CollectionHelper {

	public static Map<String, String> getMap(Properties properties) {
		Map<String, String> map= new Hashtable<String, String>();
		for (Entry<Object, Object> entry : properties.entrySet()) {
			map.put(entry.getKey().toString(), entry.getValue().toString());
		}
		return map;
	}

	public static Properties getProperties(Map<String, String> map) {
		Properties properties= new Properties();
		properties.putAll(map);
		return properties;
	}

	public static Properties getProperties(Set<String> set) {
		Properties properties= new Properties();
		for (String setEntry : set) {
			properties.put(setEntry, "");
		}
		return properties;
	}

}
