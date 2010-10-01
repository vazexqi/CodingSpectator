package edu.illinois.refactoringwatcher.monitor.prefs;

import java.util.Date;

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

	private static long getPreferenceLongValue(String key) {
		return getPreferenceStore().getLong(key);
	}

	private static String getPreferenceStringValue(String key) {
		return getPreferenceStore().getString(key);
	}

	private static void setPreferenceValue(String key, long value) {
		getPreferenceStore().setValue(key, value);
	}

	private static void setPreferenceValue(String key, String value) {
		getPreferenceStore().setValue(key, value);
	}

	private static boolean isUUIDSet() {
		return !("".equals(getPreferenceStringValue(Messages.WorkbenchPreferencePage_UUIDFieldPreferenceKey))); //$NON-NLS-1$
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
		return getPreferenceStringValue(Messages.WorkbenchPreferencePage_UUIDFieldPreferenceKey);
	}

	public synchronized long getLastUploadTime() {
		return getPreferenceLongValue(Messages.PrefsFacade_LastUploadTimeKey);
	}

	public synchronized void updateLastUploadTime() {
		setPreferenceValue(Messages.PrefsFacade_LastUploadTimeKey, new Date().getTime());
	}

}
