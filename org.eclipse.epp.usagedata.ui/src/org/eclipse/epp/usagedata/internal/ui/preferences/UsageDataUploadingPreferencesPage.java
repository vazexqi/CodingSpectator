/*******************************************************************************
 * Copyright (c) 2007 The Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    The Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.epp.usagedata.internal.ui.preferences;

import java.util.Date;

import org.eclipse.epp.usagedata.internal.gathering.UsageDataCaptureActivator;
import org.eclipse.epp.usagedata.internal.gathering.settings.UsageDataCaptureSettings;
import org.eclipse.epp.usagedata.internal.recording.UsageDataRecordingActivator;
import org.eclipse.epp.usagedata.internal.recording.settings.UsageDataRecordingSettings;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import com.ibm.icu.text.MessageFormat;

public class UsageDataUploadingPreferencesPage extends PreferencePage
	implements IWorkbenchPreferencePage {

	private static final int MILLISECONDS_IN_ONE_DAY = 24 * 60 * 60 * 1000;

	private static final long MINIMUM_PERIOD_IN_DAYS = UsageDataRecordingSettings.PERIOD_REASONABLE_MINIMUM / MILLISECONDS_IN_ONE_DAY;
	private static final long MAXIMUM_PERIOD_IN_DAYS = 90;
	
	private Text uploadPeriodText;
	private Label label;
	private Text lastUploadText;

	private Button askBeforeUploadingCheckbox;

	private Button uploadNowButton;
	
	IPropertyChangeListener capturePropertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if (UsageDataCaptureSettings.CAPTURE_ENABLED_KEY.equals(event.getProperty())) {
				updateButtons();
				return;
			}
		}		
	};

	IPropertyChangeListener recordingPropertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {			
			if (UsageDataRecordingSettings.ASK_TO_UPLOAD_KEY.equals(event.getProperty())) {
				getControl().getDisplay().syncExec(new Runnable() {
					public void run() {
						updateAskToUploadCheckbox();
					}
				});
				return;
			}

			if (UsageDataRecordingSettings.UPLOAD_PERIOD_KEY.equals(event.getProperty())) {
				getControl().getDisplay().syncExec(new Runnable() {
					public void run() {
						updateUploadPeriodText();
					}
				});				
				return;
			}
			
			if (UsageDataRecordingSettings.LAST_UPLOAD_KEY.equals(event.getProperty())) {
				getControl().getDisplay().syncExec(new Runnable() {
					public void run() {
						updateLastUploadText();
					}
				});
				return;
			}
		}		
	};

	public UsageDataUploadingPreferencesPage() {
		setDescription(Messages.UsageDataUploadingPreferencesPage_0); 
		setPreferenceStore(UsageDataRecordingActivator.getDefault().getPreferenceStore());
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		getCapturePreferenceStore().addPropertyChangeListener(capturePropertyChangeListener);
		getPreferenceStore().addPropertyChangeListener(recordingPropertyChangeListener);
	}
	
	@Override
	public void dispose() {
		getCapturePreferenceStore().removePropertyChangeListener(capturePropertyChangeListener);
		getPreferenceStore().removePropertyChangeListener(recordingPropertyChangeListener);
		super.dispose();
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		composite.setLayout(new GridLayout());
		
		createGeneralInformationArea(composite);
		createUploadingArea(composite);
		createButtonsArea(composite);
		
		Label filler = new Label(parent, SWT.NONE);
		filler.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true));
		
		initialize();
		
		return composite;
	}

	/*
	 * Note that this method expects to be run in the UI Thread.
	 */
	private void initialize() {
		updateAskToUploadCheckbox();		
		updateUploadPeriodText();		
		updateLastUploadText();
		updateButtons();
	}

	/*
	 * Note that this method expects to be run in the UI Thread.
	 */
	private void updateLastUploadText() {
		lastUploadText.setText(getLastUploadDateAsString());
	}


	/*
	 * Note that this method expects to be run in the UI Thread.
	 */
	private void updateUploadPeriodText() {
		uploadPeriodText.setText(String.valueOf(getRecordingPreferences().getLong(UsageDataRecordingSettings.UPLOAD_PERIOD_KEY) / MILLISECONDS_IN_ONE_DAY));
	}


	/*
	 * Note that this method expects to be run in the UI Thread.
	 */
	private void updateAskToUploadCheckbox() {
		askBeforeUploadingCheckbox.setSelection(getRecordingPreferences().getBoolean(UsageDataRecordingSettings.ASK_TO_UPLOAD_KEY));
	}
	
	/*
	 * Note that this method expects to be run in the UI Thread.
	 */
	private void updateButtons() {
		uploadNowButton.setEnabled(getCapturePreferenceStore().getBoolean(UsageDataCaptureSettings.CAPTURE_ENABLED_KEY));
	}


	private IPreferenceStore getCapturePreferenceStore() {
		return UsageDataCaptureActivator.getDefault().getPreferenceStore();
	}


	/*
	 * Note that this method expects to be run in the UI Thread.
	 */
	@Override
	public boolean performOk() {		
		getRecordingPreferences().setValue(UsageDataRecordingSettings.ASK_TO_UPLOAD_KEY, askBeforeUploadingCheckbox.getSelection());		
		getRecordingPreferences().setValue(UsageDataRecordingSettings.UPLOAD_PERIOD_KEY, Long.valueOf(uploadPeriodText.getText()) * MILLISECONDS_IN_ONE_DAY);
		
		return super.performOk();
	}
	
	/*
	 * Note that this method expects to be run in the UI Thread.
	 */
	@Override
	public boolean isValid() {
		if (!isValidUploadPeriod(uploadPeriodText.getText())) return false;
		return true;
	}

	/*
	 * Note that this method expects to be run in the UI Thread.
	 */
	@Override
	protected void performDefaults() {
		askBeforeUploadingCheckbox.setSelection(getRecordingPreferences().getDefaultBoolean(UsageDataRecordingSettings.ASK_TO_UPLOAD_KEY));
		uploadPeriodText.setText(String.valueOf(getRecordingPreferences().getDefaultLong(UsageDataRecordingSettings.UPLOAD_PERIOD_KEY) / MILLISECONDS_IN_ONE_DAY));

		updateLastUploadText();

		super.performDefaults();
	}

	/*
	 * Note that this method expects to be run in the UI Thread.
	 */
	private void createGeneralInformationArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
		
		composite.setLayout(new GridLayout());
				
		askBeforeUploadingCheckbox = new Button(composite, SWT.CHECK | SWT.LEFT);
		askBeforeUploadingCheckbox.setText(Messages.UsageDataUploadingPreferencesPage_1);  
	}


	/*
	 * Note that this method expects to be run in the UI Thread.
	 */
	private void createUploadingArea(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setText(Messages.UsageDataUploadingPreferencesPage_2); 
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		group.setLayout(new GridLayout(3, false));

		// Create the layout that will be used by all the fields.
		GridData fieldLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		fieldLayoutData.horizontalIndent = FieldDecorationRegistry.getDefault().getMaximumDecorationWidth();
			
		createUploadPeriodField(group);
		createLastUploadField(group);
		createUploadUrlField(group);
	}
		
	/*
	 * Note that this method expects to be run in the UI Thread.
	 */
	private void createUploadPeriodField(Group composite) {
		Label label = new Label(composite, SWT.NONE);
		label.setText(Messages.UsageDataUploadingPreferencesPage_3); 
		
		uploadPeriodText = new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.RIGHT);
		uploadPeriodText.setTextLimit(2);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gridData.horizontalIndent = FieldDecorationRegistry.getDefault().getMaximumDecorationWidth();
		gridData.horizontalSpan = 1;
		GC gc = new GC(uploadPeriodText.getDisplay());
		gc.setFont(uploadPeriodText.getFont());
		gridData.widthHint = gc.stringExtent(String.valueOf(MAXIMUM_PERIOD_IN_DAYS)).x;
		gc.dispose();
		uploadPeriodText.setLayoutData(gridData);
		
		new Label(composite, SWT.NONE).setText(Messages.UsageDataUploadingPreferencesPage_4); 
		
		final ControlDecoration rangeErrorDecoration = new ControlDecoration(uploadPeriodText, SWT.LEFT | SWT.TOP);
		rangeErrorDecoration.setDescriptionText(MessageFormat.format(Messages.UsageDataUploadingPreferencesPage_5, new Object[] {MINIMUM_PERIOD_IN_DAYS, MAXIMUM_PERIOD_IN_DAYS})); 
		rangeErrorDecoration.setImage(getErrorImage());
		rangeErrorDecoration.hide();
		
		uploadPeriodText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String contents = uploadPeriodText.getText();
				if (isValidUploadPeriod(contents))
					rangeErrorDecoration.hide();
				else {
					rangeErrorDecoration.show();
				}
				updateApplyButton();
				getContainer().updateButtons();
			}
		});
		if (System.getProperty(UsageDataRecordingSettings.UPLOAD_PERIOD_KEY) != null) {
			addOverrideWarning(uploadPeriodText);
		}
	}
	
	private boolean isValidUploadPeriod(String text) {
		try {
			long value = Long.parseLong(text);
			if (value < MINIMUM_PERIOD_IN_DAYS)
				return false;
			if (value > MAXIMUM_PERIOD_IN_DAYS)
				return false;
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
		
	private Image getErrorImage() {
		return FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage();
	}
	
	/*
	 * Note that this method expects to be run in the UI Thread.
	 */
	private void createLastUploadField(Group composite) {
		label = new Label(composite, SWT.NONE);
		label.setText(Messages.UsageDataUploadingPreferencesPage_6); 
		
		lastUploadText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		lastUploadText.setEnabled(false);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gridData.horizontalIndent = FieldDecorationRegistry.getDefault().getMaximumDecorationWidth();
		gridData.horizontalSpan = 2;
		lastUploadText.setLayoutData(gridData);
	}
	
	/*
	 * The Upload URL is not expected to change during execution, so
	 * we make not consideration for changes while the preferences
	 * page is open.
	 * 
	 * Note that this method expects to be run in the UI Thread.
	 */
	private void createUploadUrlField(Group composite) {
		label = new Label(composite, SWT.NONE);
		label.setText(Messages.UsageDataUploadingPreferencesPage_9); 
		
		Text uploadUrlText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		uploadUrlText.setEnabled(false);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gridData.horizontalIndent = FieldDecorationRegistry.getDefault().getMaximumDecorationWidth();
		gridData.horizontalSpan = 2;
		uploadUrlText.setLayoutData(gridData);
		uploadUrlText.setText(getSettings().getUploadUrl());
	}
	
	/*
	 * Note that this method expects to be run in the UI Thread.
	 */
	private void createButtonsArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		composite.setLayout(new RowLayout());

		createUploadNowButton(composite);
	}

	/*
	 * Note that this method expects to be run in the UI Thread.
	 */
	protected IWorkbenchPage getPage() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}


	protected UsageDataRecordingSettings getSettings() {
		return UsageDataRecordingActivator.getDefault().getSettings();
	}


	/*
	 * Note that this method expects to be run in the UI Thread.
	 */
	private void createUploadNowButton(Composite composite) {
		uploadNowButton = new Button(composite, SWT.PUSH);
		uploadNowButton.setText(Messages.UsageDataUploadingPreferencesPage_7); 
		uploadNowButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				UsageDataRecordingActivator.getDefault().getUploadManager().startUpload();
			}
		});
	}
	
	/*
	 * Note that this method expects to be run in the UI Thread.
	 */
	private void addOverrideWarning(Control control) {
		FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_WARNING);
		ControlDecoration warning = new ControlDecoration(control, SWT.BOTTOM | SWT.LEFT);
		warning.setImage(decoration.getImage());
		warning.setDescriptionText(Messages.UsageDataUploadingPreferencesPage_8); 
	}

	private String getLastUploadDateAsString() {
		long time = getRecordingSettings().getLastUploadTime();
		Date date = new Date(time);
		return date.toString();
	}


	private IPreferenceStore getRecordingPreferences() {
		return UsageDataRecordingActivator.getDefault().getPreferenceStore();
	}

	private UsageDataRecordingSettings getRecordingSettings() {
		return UsageDataRecordingActivator.getDefault().getSettings();
	}
}