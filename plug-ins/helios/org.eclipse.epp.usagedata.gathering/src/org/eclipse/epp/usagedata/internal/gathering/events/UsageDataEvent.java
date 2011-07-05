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

/**
 * The {@link UsageDataEvent} class captures information about a single
 * event. Once created, instances of this class cannot be modified.
 * 
 * @author Wayne Beaton
 *
 */
public class UsageDataEvent {

	/**
	 * The {@link #what} field describes the event that has occurred. It
	 * is dependent on the kind of thing that caused the event. As a 
	 * rule of thumb, the value indicates that something has
	 * already happened (e.g. "activated", "loaded", "clicked").
	 */
	public final String what;
	
	/**
	 * The {@link #kind} field describes the kind of thing that caused
	 * the event (e.g. "view", "workbench", "menu", "bundle").
	 */
	public final String kind;
	
	/**
	 * The {@link #description} field provides additional, kind-specific
	 * information. An event describing the activation of a view might,
	 * for example, provide the name of the view in this field.
	 */
	public final String description;
	
	/**
	 * The {@link #bundleId} field contains symbolic name of the bundle that
	 * owns the thing that caused the event.
	 */
	public final String bundleId;
	
	/**
	 * The {@link #bundleVersion} field contains the version of the bundle
	 * that owns the thing that caused the event.
	 */
	public String bundleVersion;
	
	/**
	 * The {@link #when} field contains a time stamp, expressed as
	 * milliseconds in UNIX time (using <code>System.currentTimeMillis()</code>);
	 */
	public final long when;

	public UsageDataEvent(String what, String kind, String description, String bundleId,
			String bundleVersion, long when) {
				this.what = what;
				this.kind = kind;
				this.description = description;
				this.bundleId = bundleId;
				this.bundleVersion = bundleVersion;
				this.when = when;
	}

}
