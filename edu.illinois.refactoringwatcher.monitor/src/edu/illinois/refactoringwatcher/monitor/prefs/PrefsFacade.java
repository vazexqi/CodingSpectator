package edu.illinois.refactoringwatcher.monitor.prefs;

import org.eclipse.jface.preference.IPreferenceStore;

import edu.illinois.refactoringwatcher.monitor.Activator;
import edu.illinois.refactoringwatcher.monitor.Messages;

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

	private static String getPreferenceValue(String key) {
		return getPreferenceStore().getString(key);
	}

	private static void setPreferenceValue(String key, String value) {
		getPreferenceStore().setValue(key, value);
	}

	private static boolean isUUIDSet() {
		return !("".equals(getPreferenceValue(Messages.WorkbenchPreferencePage_UUIDFieldPreferenceKey)));
	}

	private static void setUUIDLazily() {
		if (!isUUIDSet()) {
			setUUID(UUIDGenerator.generateID());
		}
	}

	public static void setUUID(String uiud) {
		setPreferenceValue(Messages.WorkbenchPreferencePage_UUIDFieldPreferenceKey, uiud);
	}

	public static String getAndSetUUIDLazily() {
		setUUIDLazily();
		return getPreferenceValue(Messages.WorkbenchPreferencePage_UUIDFieldPreferenceKey);
	}

}
