public class Jloops {
	public static void main(String[] args) {
		// 1. Create an array/collection of numbers (initialize it with any number of
		// numbers (more than 1) in numerical order,
		// with or without duplicates)
		int arr[] = new int[20];
		System.out.println("All numbers in array:");
		for (int i = 0; i < arr.length; i++) {
			arr[i] = i;
		}
		// 2. Create a loop that loops over each number and shows their value.
		for (int i = 0; i < arr.length; i++) {
			System.out.print(arr[i] + ", ");
		}
		System.out.println("\nEven numbers using iterator:");
		// 3. Have the loop output only even numbers regardless of how long the
		// array/collection is.
		for (int i = 0; i < arr.length; i += 2) {
			System.out.print(arr[i] + ", ");
		}
		System.out.println("\nEven numbers using case statement:");
		// OR
		for (int i = 0; i < arr.length; i++) {
			if (i % 2 == 0) {
				System.out.print(arr[i] + ", ");
			}

		}
		// 4. Briefly explain how you achieved the correct output.

		/*
		 * I did it in two different ways: the first way I iterated over the array while
		 * starting with an even number (0) and went up by 2 each time in the loop. This
		 * way worked because all the numbers were filled linearly The other way was to
		 * check if i was currently divisible by 2 and if so display it, if not just
		 * loop again
		 */

	}
}