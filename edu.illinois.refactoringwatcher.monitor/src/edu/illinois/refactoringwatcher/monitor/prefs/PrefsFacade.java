package edu.illinois.refactoringwatcher.monitor.prefs;

import org.eclipse.jface.preference.IPreferenceStore;

import edu.illinois.refactoringwatcher.monitor.Activator;
import edu.illinois.refactoringwatcher.monitor.Messages;

/**
 * This class provides the facade to access the preference store. Since this is a shared resource we
 * need to protect it and we do so using a singleton.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 */
public class PrefsFacade {

	// This method of providing a thread safe singleton comes from 
	// http://www.ibm.com/developerworks/java/library/j-dcl.html
	private static PrefsFacade instance= new PrefsFacade();

	private PrefsFacade() {

	}

	public static PrefsFacade getInstance() {
		return instance;
	}

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

	private void setUUIDLazily() {
		if (!isUUIDSet()) {
			setUUID(UUIDGenerator.generateID());
		}
	}

	private synchronized void setUUID(String uiud) {
		setPreferenceValue(Messages.WorkbenchPreferencePage_UUIDFieldPreferenceKey, uiud);
	}

	public synchronized String getAndSetUUIDLazily() {
		setUUIDLazily();
		return getPreferenceValue(Messages.WorkbenchPreferencePage_UUIDFieldPreferenceKey);
	}

}
