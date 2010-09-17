package edu.illinois.refactoringwatcher.monitor.prefs;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.illinois.refactoringwatcher.monitor.Activator;
import edu.illinois.refactoringwatcher.monitor.Messages;

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
		PrefsFacade.generateUUIDIfDoesNotExist();
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
