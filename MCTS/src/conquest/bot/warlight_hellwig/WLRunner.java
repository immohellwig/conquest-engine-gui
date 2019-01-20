package conquest.bot.warlight_hellwig;

import java.util.Arrays;

import conquest.engine.GameResult;

//import conquest.bot.state.GameState;

public class WLRunner {
	static int[] wins = new int[3];
	public static void main(String[] args) {
		GameResult result;
		for (int i = 1 ; i <= 100 ; i++) {
			result = MCTSBot.runInternal(true);
			wins[result.getWinner()]++;
			System.err.println("--------------------");
			System.err.println(Arrays.toString(wins));
			System.err.println("--------------------");
		}
			
	}
}
