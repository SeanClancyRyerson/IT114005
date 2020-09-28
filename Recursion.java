public class Recursion {

	public static int sum(int num) {
		if (num > 0) {
			return num + sum(num - 1);
		}
		return 0;
	}

	public static int iterSum(int num) {
		int sum = 0;
		for (int i = 0; i <= num; i++) {
			sum += i;
		}
		return sum;
	}

	public static void main(String[] args) {
		System.out.println(sum(100));
		System.out.println(iterSum(100));
	}
}