/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.helpers;

import org.eclipse.jface.action.IAction;

/**
 * 
 * @author Stas Negara
 * 
 */
public class ViewerHelper {

	public static void initAction(IAction action, String actionText, String actionToolTipText, boolean isEnabled, boolean isToggable, boolean initState) {
		action.setText(actionText);
		action.setToolTipText(actionToolTipText);
		action.setEnabled(isEnabled);
		if (isToggable) {
			action.setChecked(initState);
		}
	}

}
