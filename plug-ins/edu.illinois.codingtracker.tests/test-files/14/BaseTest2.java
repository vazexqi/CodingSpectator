package p1.p2;

public class BaseTest2 {

	private static final int deltaIndex = 5;
	private final int sumFactor = 3;

	public void method2(){
		Runnable myRunnable = new MyRunnable();
		int k=deltaIndex;
		int product = k * k;
		int j = sumFactor + product;
	}
	
}
