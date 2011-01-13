package edu.illinois.codingspectator.tests;

public class ValidMoveStaticFieldTest extends MoveStaticMemberTest {

	@Override
	protected String getDestinationType() {
		return "edu.illinois.codingspectator.C3";
	}

	@Override
	protected String getSelectedMember() {
		return "field1";
	}

	@Override
	public void prepareRefactoring() {
		invokeRefactoring(7, 18, 25 - 19);
	}

	@Override
	String getTestFileName() {
		return "MoveStaticMemberTestFile";
	}

}
