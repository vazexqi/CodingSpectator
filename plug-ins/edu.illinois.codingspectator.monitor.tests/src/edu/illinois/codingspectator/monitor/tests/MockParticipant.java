/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.tests;

public class MockParticipant {

	String username;

	String password;

	public MockParticipant(String username, String password) {
		this.username= username;
		this.password= password;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

}
