/*******************************************************************************
 * Copyright (c) 2008 The Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    The Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.epp.usagedata.internal.ui.preview;

import org.eclipse.epp.usagedata.internal.recording.filtering.FilterUtils;
import org.eclipse.epp.usagedata.internal.recording.filtering.PreferencesBasedFilter;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Shell;

public class AddFilterDialog {

	private final PreferencesBasedFilter filter;

	public AddFilterDialog(PreferencesBasedFilter filter) {
		this.filter = filter;
	}

	public void prompt(Shell shell, String suggestion) {
		InputDialog dialog = new InputDialog(shell, Messages.AddFilterDialog_0, Messages.AddFilterDialog_1,suggestion, getValidator()); 
		dialog.open();
		if (dialog.getReturnCode() != InputDialog.OK) return;
		
		filter.addPattern(dialog.getValue().trim());
	}

	IInputValidator getValidator() {
		return new IInputValidator() {
			public String isValid(String pattern) {
				if (pattern == null) return null;
				pattern = pattern.trim();
				if (pattern.length() == 0) return null;
				if (alreadyHasPattern(pattern)) return Messages.AddFilterDialog_2; 
				if (!FilterUtils.isValidBundleIdPattern(pattern)) return Messages.AddFilterDialog_3; 
				return null;
			}
		};
	}

	boolean alreadyHasPattern(String pattern) {
		return filter.includesPattern(pattern);
	}

}
