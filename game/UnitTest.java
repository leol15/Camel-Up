

public class UnitTest {
	public static void main(String[] args) {
		TestRollDie();
	}

	public static void TestRollDie() {
		CamelUp CU = new CamelUp();
		System.out.println(CU);
		CU.rollDie();
		System.out.println(CU);
		CU.rollDie();
		System.out.println(CU);
		CU.rollDie();
		System.out.println(CU);
	}
}