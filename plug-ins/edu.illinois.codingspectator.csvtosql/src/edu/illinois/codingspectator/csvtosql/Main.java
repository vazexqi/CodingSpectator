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
public class Main {
	//mysql
//	static String account= "root";
//	static String password= "root";
//	static String server= "localhost";

	static String account= "SA";

	static String password= "";

	static String server= "db_file";

	public static void main(String[] args) throws Throwable {
		MapPersister mapPersister= new MapPersister(server, account, password);
		String path= System.getenv("CS_CSV");
		mapPersister.loadCsvToMySql(path);
		mapPersister.answer1();
		System.out.println("******");
		mapPersister.answer2();
		System.out.println("******");
		mapPersister.answer3();
		mapPersister.shutdown();
	}
}
