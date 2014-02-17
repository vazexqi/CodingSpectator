/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.tests;

/**
 * 
 * @author Mohsen Vakilian
 * 
 */
public class MockParticipantFactory {

	private static MockParticipant[] participants= new MockParticipant[] {
			new MockParticipant(Messages.MockParticipantFactory_MockParticipantOneUsername, Messages.MockParticipantFactory_MockParticipantOnePassword),
			new MockParticipant(Messages.MockParticipantFactory_MockParticipantTwoUsername, Messages.MockParticipantFactory_MockParticipantTwoPassword) };

	public static MockParticipant getMockParticipant(int id) {
		return participants[id];
	}

}
