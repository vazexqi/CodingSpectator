/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.csvtosql;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.supercsv.io.CsvMapReader;
import org.supercsv.prefs.CsvPreference;

/**
 * 
 * @author Balaji Ambresh Rajkumar
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class MapPersister {
	private static final int VARCHAR_SIZE= 100000;

	Connection connection;

	private Statement statement;

	private static final char ID_QUOTE= '\"';

	String dbName= "CodingSpectator";

	String tableName= "ALL_DATA";

	public MapPersister(String server, String account, String password)
			throws ClassNotFoundException, SQLException {
		// Load the HSQL Database Engine JDBC driver
		// hsqldb.jar should be in the class path or made part of the current jar
		Class.forName("org.hsqldb.jdbcDriver");
		// connect to the database.   This will load the db files and start the
		// database if it is not alread running.
		// db_file_name_prefix is used to open or create files that hold the state
		// of the db.
		// It can contain directory names relative to the
		// current working directory
		connection= DriverManager.getConnection("jdbc:hsqldb:file:" + server + ";shutdown=true", account, password);
		statement= connection.createStatement();
	}

	public void shutdown() throws SQLException {
		connection.close();
	}

	private String IDQuoted(String name) {
		return ID_QUOTE + name + ID_QUOTE;
	}

	public void loadCSVToSQL(String filePath) throws IOException, SQLException {
		CsvMapReader reader= new CsvMapReader(new FileReader(filePath), CsvPreference.EXCEL_PREFERENCE);
		String[] csvHeader= reader.getCSVHeader(true);

		//List<String> toRemove= Arrays.asList("code-snippet", "selection-text");

		List<String> toRemove= Arrays.asList("");

		List<String> columnHeaders= Arrays.asList(csvHeader);
		List<String> filteredColumnHeaders= filterUnnecessaryColumns(columnHeaders, toRemove);
		List<String> nulledColumnHeaders= nullUnnecessaryColumns(columnHeaders, toRemove);

		if (!(nulledColumnHeaders.size() == columnHeaders.size()))
			throw new RuntimeException("Nulled column does not have same number of elements as original header");

		createTable(tableName, filteredColumnHeaders);

		connection.setAutoCommit(false);

		StringBuilder insertString= new StringBuilder("INSERT INTO " + IDQuoted(tableName) + " values (");
		for (int i= 1; i <= filteredColumnHeaders.size(); i++) {
			insertString.append("?");
			if (i < filteredColumnHeaders.size()) {
				insertString.append(",");
			}
		}
		insertString.append(")");
		PreparedStatement preparedStatement= connection.prepareStatement(insertString.toString());


		Map<String, String> row;
		int progress= 0;
		while ((row= reader.read(nulledColumnHeaders.toArray(new String[] {}))) != null) {
			int index= 1;
			for (String key : filteredColumnHeaders) {
				String value= row.get(key);
				if (key.toUpperCase().equals("TIMESTAMP")) {
					preparedStatement.setLong(index, Long.valueOf(value));
				} else {
					if (value.length() > VARCHAR_SIZE) {
						value= value.substring(0, VARCHAR_SIZE);
						System.err.println("\n>>>Truncated value at: " + key + " is: " + value + " and exceeds HSQLDB data capacity!");
					}
					preparedStatement.setString(index, value);
				}

				index++;
			}
			preparedStatement.execute();

			++progress;
			if (progress % 100 == 0) {
				System.out.print('>');
				if (progress % 8000 == 0) {
					progress= 0;
					System.out.println();
					connection.commit();
				}
			}
		}

		System.out.println();
		connection.commit();
		reader.close();
	}

	private List<String> nullUnnecessaryColumns(List<String> columnHeaders, List<String> toRemove) {
		ArrayList<String> copy= new ArrayList<String>();
		for (String string : columnHeaders) {
			if (toRemove.contains(string))
				copy.add(null);
			else
				copy.add(string);
		}
		return copy;
	}

	private ArrayList<String> filterUnnecessaryColumns(List<String> columnHeaders, List<String> toRemove) {
		ArrayList<String> copy= new ArrayList<String>(columnHeaders);
		copy.removeAll(toRemove);
		return copy;
	}

	protected void createTable(String tableName, List<String> fields) throws SQLException {
		statement.execute("DROP TABLE " + tableName + " IF EXISTS");

		StringBuilder command= new StringBuilder("CREATE TABLE ");
		command.append(IDQuoted(tableName)).append('(');
		for (String field : fields) {
			command.append(defineColumnNameAndType(field)).append(',');
		}
		int length= command.length();
		command.replace(length - 1, length, ")");
//		System.out.println("command is: " + command);
		statement.executeUpdate(command.toString());
	}

	protected String defineColumnNameAndType(String field) {
		StringBuilder fieldDefinition= new StringBuilder();

		fieldDefinition.append(IDQuoted(field)).append(' ');
		if (field.toUpperCase().equals("TIMESTAMP")) {
			fieldDefinition.append("BIGINT");
		} else {
			fieldDefinition.append("VARCHAR(" + VARCHAR_SIZE + ")");
		}
		return fieldDefinition.toString();
	}

	public ResultSet executeQuery(String query) throws SQLException {
		return statement.executeQuery(query);
	}
}
