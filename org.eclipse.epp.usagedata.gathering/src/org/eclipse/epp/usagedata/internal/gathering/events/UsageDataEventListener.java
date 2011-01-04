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
package org.eclipse.epp.usagedata.internal.gathering.events;

import org.eclipse.epp.usagedata.internal.gathering.services.UsageDataService;

/**
 * Implementors of this interface subscribe to the {@link UsageDataService} for
 * notification of usage data events.
 * 
 * @author Wayne Beaton
 *
 */
public interface UsageDataEventListener {

	/**
	 * This method is invoked to deliver an event
	 * to the receiver. 
	 * 
	 * @param event instance of {@link UsageDataEvent}.
	 */
	void accept(UsageDataEvent event);
}
