package test;

import java.util.Random;

public class testRandom {

	public static void main(String[] args) {
		int seed = new Random().nextInt();
		Random rand = new Random(seed);
		Random randcopy = new Random(seed);
		int i = 0;
		while (i++ < 5) {
			System.out.println(rand.nextDouble() + " | " + randcopy.nextDouble());
		}
		rand.setSeed(seed);
		while (i++ < 10) {
			System.out.println(rand.nextDouble() + " | " + randcopy.nextDouble());
		}
	}

}
