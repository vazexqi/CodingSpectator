/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.tests;

public class MockParticipantFactory {

	private static MockParticipant[] participants= new MockParticipant[] { new MockParticipant("test.codingspectator", "test.codingspectator") };

	public static MockParticipant getMockParticipant(int id) {
		return participants[id];
	}
}
