package org.eclipse.jdt.internal.corext.refactoring.codingspectator;

/**
 * 
 * @author nchen
 * @author Mohsen Vakilian
 * 
 */
abstract public class WatchedMoveProcessor extends WatchedProcessor {

	protected Object[] getElements() {
		return WatchedMoveProcessor.this.getElements();
	}

}
