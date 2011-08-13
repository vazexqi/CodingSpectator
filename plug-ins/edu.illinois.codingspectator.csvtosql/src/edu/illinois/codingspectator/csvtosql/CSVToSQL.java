/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.csvtosql;

/**
 * 
 * @author Balaji Ambresh Rajkumar
 * @author Mohsen Vakilian
 * 
 */
public class CSVToSQL {

	static String account= "SA";

	static String password= "";

	static String server= "db_file";

	public static void main(String[] args) throws Throwable {
		MapPersister mapPersister= new MapPersister(server, account, password);
		String path= System.getenv("CS_CSV");
		mapPersister.loadCSVToSQL(path);
		mapPersister.shutdown();
	}

}

