/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.logstocsv;

import java.io.IOException;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;

import edu.illinois.codingspectator.efs.EFSFile;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class ConvertLogsToCSV {

	public static void main(String[] args) throws CoreException, IOException {
		EFSFile rootCodingSpectatorDataFolder= new EFSFile(args[1]);
		String codingspectatorDataCSVFileName= args[2];

		Collection<Event> events= new CodingSpectatorDataExtractor(rootCodingSpectatorDataFolder).extractData();
		new CodingSpectatorCSVWriter(codingspectatorDataCSVFileName).writeToCSV(events);

		if (args.length >= 4) {
			String checksAfterRefactoringsCSVFileName= args[3];
			new CheckAndRefactoringPatternFinder(events, checksAfterRefactoringsCSVFileName).reportChecksAfterRefactorings();
		}
	}

}
