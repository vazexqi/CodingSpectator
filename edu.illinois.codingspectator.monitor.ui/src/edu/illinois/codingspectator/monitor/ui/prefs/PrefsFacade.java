/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.ui.prefs;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ltk.core.refactoring.codingspectator.RunningModes;

import edu.illinois.codingspectator.monitor.core.submission.LocalSVNManager;
import edu.illinois.codingspectator.monitor.ui.Activator;
import edu.illinois.codingspectator.monitor.ui.Messages;
import edu.illinois.codingspectator.monitor.ui.submission.Submitter;

/**
 * This class provides the facade to access the preference store. Since this is a shared resource we
 * need to protect it and we do so using a singleton.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 */
public class PrefsFacade {

	private static final String TESTING_UUID= "00000000-0000-0000-0000-000000000000"; //$NON-NLS-1$

	// This method of providing a thread safe singleton comes from 
	// http://www.ibm.com/developerworks/java/library/j-dcl.html
	private static PrefsFacade instance= new PrefsFacade();

	private PrefsFacade() {

	}

	public static PrefsFacade getInstance() {
		return instance;
	}

	public IPreferenceStore getPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	private String getPreferenceStringValue(String key) {
		return getPreferenceStore().getString(key);
	}

	private void setPreferenceValue(String key, String value) {
		getPreferenceStore().setValue(key, value);
	}

	private boolean isUUIDSet() {
		return !(getPreferenceStringValue(Messages.WorkbenchPreferencePage_UUIDFieldPreferenceKey)).isEmpty();
	}

	private void setUUIDLazily() {
		if (!isUUIDSet()) {
			String generatedID;
			if (RunningModes.isInProductionMode()) {
				generatedID= new LocalSVNManager(Submitter.WATCHED_DIRECTORY).getSVNWorkingCopyRepositoryUUID();
				if (generatedID.isEmpty()) {
					generatedID= UUIDGenerator.generateID();
				}
			} else {
				generatedID= TESTING_UUID;
			}
			setUUID(generatedID);
		}
	}

	private synchronized void setUUID(String uiud) {
		setPreferenceValue(Messages.WorkbenchPreferencePage_UUIDFieldPreferenceKey, uiud);
	}

	public synchronized String getAndSetUUIDLazily() {
		setUUIDLazily();
		return getPreferenceStringValue(Messages.WorkbenchPreferencePage_UUIDFieldPreferenceKey);
	}

	public synchronized boolean getForcedAutoUpdatePref() {
		return getPreferenceStore().getBoolean(Messages.PrefsFacade_ForcedAutomaticUpdateHasBeenSetKey);
	}

	public synchronized void setForcedAutoUpdatePref(boolean value) {
		getPreferenceStore().setValue(Messages.PrefsFacade_ForcedAutomaticUpdateHasBeenSetKey, value);
	}

	public synchronized long getLastUploadTime() throws ParseException {
		return getDateFormat().parse(getPreferenceStringValue(Messages.PrefsFacade_LastUploadTimeKey)).getTime();
	}

	public synchronized void updateLastUploadTime() {
		setPreferenceValue(Messages.PrefsFacade_LastUploadTimeKey, getDateFormat().format(new Date()));
	}

	private DateFormat getDateFormat() {
		return DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
	}

}
