package edu.illinois.refactoringwatcher.monitor.prefs;

import org.eclipse.jface.preference.IPreferenceStore;

import edu.illinois.refactoringwatcher.monitor.Activator;
import edu.illinois.refactoringwatcher.monitor.Messages;
import edu.illinois.refactoringwatcher.monitor.idgen.UUIDGenerator;

/**
 * TODO: Get rid of the netid accessors.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class PrefsFacade {

	private static IPreferenceStore getPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	public static void generateUUIDIfDoesNotExist() {
		IPreferenceStore preferenceStore= getPreferenceStore();
		String UUID= preferenceStore.getString(Messages.WorkbenchPreferencePage_UUIDFieldPreferenceKey);
		if ("".equals(UUID)) {
			String newUUID= UUIDGenerator.generateID();
			preferenceStore.setValue(Messages.WorkbenchPreferencePage_UUIDFieldPreferenceKey, newUUID);
			setUUID(newUUID);
		}
	}

	public static void setUUID(String uiud) {
		getPreferenceStore().setValue(Messages.WorkbenchPreferencePage_UUIDFieldPreferenceKey, uiud);
	}

	public static void setNetid(String netid) {
		getPreferenceStore().setValue(Messages.WorkbenchPreferencePage_netidFieldPreferenceKey, netid);

	}

	private static String getPreferenceValue(String key) {
		return getPreferenceStore().getString(key);
	}

	public static String getNetid() {
		return getPreferenceValue(Messages.WorkbenchPreferencePage_netidFieldPreferenceKey);
	}

	public static String getUUID() {
		return getPreferenceValue(Messages.WorkbenchPreferencePage_UUIDFieldPreferenceKey);
	}


}
