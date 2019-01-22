package conquest.bot.warlight_hellwig;

import conquest.bot.state.Action;
import conquest.bot.state.GameState;
import mcts.Mcts;
import mcts.Runner;

//import conquest.bot.state.GameState;

public class WarlightRunner {
	static int[] wins = new int[3];
	public static void main(String[] args) {
		GameState state = new GameState();
		WarlightGame game = new WarlightGame(state);
		WarlightGenerator generator = new WarlightGenerator();
		Runner.play(game, new AggressiveBaseStrategy(), new Mcts<GameState, Action>(game, generator, new AggressiveBaseStrategy(), 1000, 3000), 100);
	}
}
