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
package org.eclipse.epp.usagedata.internal.gathering.monitors;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;

/**
 * Instances of this class perform a mapping between
 * extensions and the id (symbolic name) of the bundle that defines it.
 * The constructor takes the name of an extension point. From that,
 * it builds a mapping of all extensions to that extension point. It
 * is assumed that all extensions have an &quot;id&quot; attribute
 * that is used for the mapping.
 * <p>
 * The instance will rebuild its cache when the extension registry
 * detects a change to the extension point (i.e. when bundles are
 * added or removed).
 * </p>
 * 
 * @author Wayne Beaton
 *
 */
public class ExtensionIdToBundleMapper {
	private Map<String, String> map;
	private final String extensionPointId;
	private IRegistryChangeListener listener = new IRegistryChangeListener() {
		public void registryChanged(IRegistryChangeEvent event) {
			if (extensionsAdded(event)) {
				clearCache();
			}
		}

		private boolean extensionsAdded(IRegistryChangeEvent event) {
			for (IExtensionDelta delta : event.getExtensionDeltas()) {
				if (delta.getExtensionPoint().getUniqueIdentifier().equals(extensionPointId))
					return true;
			}
			return false;
		}
	};

	public ExtensionIdToBundleMapper(String extensionPointId) {
		this.extensionPointId = extensionPointId;
		hookListeners();
	}
	
	void hookListeners() {
		Platform.getExtensionRegistry().addRegistryChangeListener(listener);
	}
	
	public void dispose() {
		Platform.getExtensionRegistry().removeRegistryChangeListener(listener);
		clearCache();
	}		
	
	/**
	 * This method fetches the bundle that defines the extension, extensionId.
	 * Since extensions are defined in the plugin.xml, the bundle that defines
	 * it must be a singleton which means that there will only be one version of
	 * the bundle loaded. Happy day.
	 * 
	 * @param extensionId
	 * @return
	 */
	protected synchronized String getBundleId(String extensionId) {
		updateCommandToBundleMappings();
		return map.get(extensionId);
	}

	private synchronized void clearCache() {
		map = null;		
	}
	
	/**
	 * This method walks through the commands registered via the extension registry
	 * and creates the {@link #map}.
	 */
	private synchronized void updateCommandToBundleMappings() {
		if (map != null) return;
		map = new HashMap<String, String>();
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(extensionPointId);
		for (IConfigurationElement element : elements) {
			map.put(element.getAttribute("id"), element.getContributor().getName()); //$NON-NLS-1$
		}
	}
}
