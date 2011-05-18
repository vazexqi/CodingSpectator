package p1.p2;

import java.util.LinkedList;

public class BaseTest2 extends BaseBaseTest implements BaseInterface {

	public static void indirectTest(BaseTest2 baseTest2) {
		baseTest2.test();
	}

	/* (non-Javadoc)
	 * @see p1.p2.BaseInterface#method2()
	 */
	public void method2(){
		Runnable myRunnable = new MyRunnable();
		int k=deltaIndex;
		int product = k * k;
		int j = data.getSumFactor() + product;
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
	
	public void testMe(String str){
		String myString = str;
	}

	public void testGenericTypes(){
		LinkedList<String> list = new LinkedList<String>();
		list.add("aaa");
		String str = list.get(0);
	}
	
}
