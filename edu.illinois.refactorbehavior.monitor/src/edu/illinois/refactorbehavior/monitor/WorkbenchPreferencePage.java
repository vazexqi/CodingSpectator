package edu.illinois.refactorbehavior.monitor;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.illinois.refactorbehavior.monitor.idgen.UUIDGenerator;

/**
 * This is the preference page for the plug-in. It stores two values: a netid(String) and a
 * UUID(String). These values will be used in the URL to the repository to store the recorded data.
 * 
 * A single UUID is assigned to each instance of Eclipse helping us identify which machine the user
 * is working on in the event that a user programs on multiple machines.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class WorkbenchPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public WorkbenchPreferencePage() {
		super(GRID);
		noDefaultAndApplyButton();
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(Messages.WorkbenchPreferencePage_title);
		IPreferenceStore preferenceStore= Activator.getDefault().getPreferenceStore();
		generateUUIDIfDoesNotExist(preferenceStore);

	}

	private void generateUUIDIfDoesNotExist(IPreferenceStore preferenceStore) {
		String UUID= preferenceStore.getString(Messages.WorkbenchPreferencePage_UUIDFieldPreferenceKey);
		if ("".equals(UUID)) {
			String newUUID= UUIDGenerator.generateID();
			preferenceStore.setValue(Messages.WorkbenchPreferencePage_UUIDFieldPreferenceKey, newUUID);
		}
	}

	@Override
	protected void createFieldEditors() {
		addField(new StringFieldEditor(Messages.WorkbenchPreferencePage_netidFieldPreferenceKey, Messages.WorkbenchPreferencePage_netidTextField, getFieldEditorParent()));
		StringFieldEditor textfield= new StringFieldEditor(Messages.WorkbenchPreferencePage_UUIDFieldPreferenceKey, Messages.WorkbenchPreferencePage_UUIDTextField,
				getFieldEditorParent());
		textfield.setEnabled(false, getFieldEditorParent());
		addField(textfield);
	}
}
