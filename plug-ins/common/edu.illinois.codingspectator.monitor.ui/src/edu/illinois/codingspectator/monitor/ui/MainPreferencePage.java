/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.ui;

import java.text.MessageFormat;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.illinois.codingspectator.monitor.ui.prefs.PrefsFacade;

/**
 * @author Mohsen Vakilian
 * 
 */
public class MainPreferencePage extends BundlePreferencePage implements IWorkbenchPreferencePage {

	private StringFieldEditor uiudTextField;

	public MainPreferencePage() {
		super(GRID);
		noDefaultAndApplyButton();
	}

	@Override
	public void init(IWorkbench workbench) {
		IPreferenceStore preferenceStore= PrefsFacade.getInstance().getPreferenceStore();
		setPreferenceStore(preferenceStore);
		setDescription(MessageFormat.format(Messages.MainPreferencePage_PreferencePageDescription, Messages.PluginName, Messages.PluginName));
		preferenceStore.addPropertyChangeListener(new UIUDChangeListener(Messages.UploadingPreferencePage_UUIDFieldPreferenceKey));
	}

	@Override
	protected void createFieldEditors() {
		uiudTextField= addDisabledTextField(Messages.UploadingPreferencePage_UUIDFieldPreferenceKey, Messages.UploadingPreferencePage_UUIDTextField);
	}

	public class UIUDChangeListener extends PreferenceChangeListener {

		public UIUDChangeListener(String preferenceKey) {
			super(preferenceKey);
		}

		@Override
		protected StringFieldEditor getFieldEditor() {
			return uiudTextField;
		}

	}

}
