/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.ui;

import java.text.MessageFormat;

import org.eclipse.swt.SWT;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class DialogState {

	private DialogType dialogType= DialogType.FIRST_PROMPT_FOR_AUTHENTICATION_INFO;

	public enum DialogType {
		FIRST_PROMPT_FOR_AUTHENTICATION_INFO,
		PROMPT_FOR_REENTRING_AUTHENTICATION_INFO
	}

	public int getDialogType() {
		switch (dialogType) {
			case FIRST_PROMPT_FOR_AUTHENTICATION_INFO:
				return SWT.ICON_QUESTION;

			default:
				return SWT.ICON_ERROR;
		}
	}

	public String getDialogDescription() {
		switch (dialogType) {
			case FIRST_PROMPT_FOR_AUTHENTICATION_INFO:
				return MessageFormat.format(Messages.AuthenticationPrompter_DialogDescription, Messages.PluginName, Messages.PluginName);

			default:
				return MessageFormat.format(Messages.AuthenticationPrompter_DialogDescriptionForReenteringAuthenticationInfo, Messages.PluginName);
		}
	}

	public void changeState() {
		dialogType= DialogType.PROMPT_FOR_REENTRING_AUTHENTICATION_INFO;
	}

}
