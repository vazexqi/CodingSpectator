package p1.p2;

public class BaseTest2 extends BaseBaseTest implements BaseInterface {

	/* (non-Javadoc)
	 * @see p1.p2.BaseInterface#method2()
	 */
	public void method2(){
		Runnable myRunnable = new MyRunnable();
		int k=deltaIndex;
		int product = k * k;
		int j = data.sumFactor + product;
	}
	
	public BaseInterface returnMe(){
		return new BaseTest2();
	}
	
	public void test(){
		returnMe().method2();
	}

	public void pullMeUp() {
		String str="up";
	}
	
	public void multipleParameters(MultipleParameters parameterObject){
		
	}
	
}
