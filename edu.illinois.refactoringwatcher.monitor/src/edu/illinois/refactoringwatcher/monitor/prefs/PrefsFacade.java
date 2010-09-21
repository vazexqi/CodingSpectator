package edu.illinois.refactoringwatcher.monitor.prefs;

import org.eclipse.jface.preference.IPreferenceStore;

import edu.illinois.refactoringwatcher.monitor.Activator;
import edu.illinois.refactoringwatcher.monitor.Messages;
import edu.illinois.refactoringwatcher.monitor.idgen.UUIDGenerator;

/**
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

	private static String getPreferenceValue(String key) {
		return getPreferenceStore().getString(key);
	}

	public static String getUUID() {
		return getPreferenceValue(Messages.WorkbenchPreferencePage_UUIDFieldPreferenceKey);
	}

}
