package conquest.bot.warlight_hellwig;

import conquest.bot.state.GameState;
import mcts.Runner;

public class WLRunner {
	public static void main(String[] args) {
		GameState state = new GameState();
		WarlightGame game = new WarlightGame(state);
		Runner.play(game, new MCTSBot(), new AggressiveBaseStrategy(), 100);
	}
}
