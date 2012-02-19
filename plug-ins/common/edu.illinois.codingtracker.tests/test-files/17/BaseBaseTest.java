package p1.p2;

public class BaseBaseTest {

	protected static final int deltaIndex = 5;
	public static BaseBaseTest createBaseBaseTest() {
		return new BaseBaseTest();
	}

	protected BaseBaseTestData data = new BaseBaseTestData(3);

	protected BaseBaseTest() {
		super();
	}

}