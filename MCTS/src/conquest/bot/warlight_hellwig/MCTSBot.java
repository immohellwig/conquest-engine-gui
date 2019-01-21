package conquest.bot.warlight_hellwig;

import java.io.File;
import java.util.List;

import conquest.bot.BotParser;
import conquest.bot.state.Action;
import conquest.bot.state.ChooseCommand;
import conquest.bot.state.GameBot;
import conquest.bot.state.GameState;
import conquest.bot.state.MoveAction;
import conquest.bot.state.MoveCommand;
import conquest.bot.state.PlaceAction;
import conquest.bot.state.PlaceCommand;
import conquest.engine.Config;
import conquest.engine.GameResult;
import conquest.engine.RunGame;
import conquest.game.FightMode;
import conquest.game.world.Region;
import mcts.Mcts;

public class MCTSBot extends GameBot {

	Mcts<GameState, Action> mcts;

	@Override
	public ChooseCommand chooseRegion(List<Region> choosable, long timeout) {
		if (mcts == null) {
			WarlightGame game = new WarlightGame(state);
			MCTSGenerator generator = new MCTSGenerator();
			mcts = new Mcts<GameState, Action>(game,generator, new AggressiveBaseStrategy(), 1000, 5);
		}		
		return (ChooseCommand) mcts.action(state);
	}

	@Override
	public List<PlaceCommand> placeArmies(long timeout) {
		return ((PlaceAction) mcts.action(state)).commands;
	}

	@Override
	public List<MoveCommand> moveArmies(long timeout) {
		return ((MoveAction) mcts.action(state)).commands;
	}

	public static GameResult runInternal(boolean visual) {
		Config config = new Config();

		config.bot1Init = "internal:conquest.bot.warlight_hellwig.MCTSBot";

		config.bot2Init = "internal:conquest.bot.warlight_hellwig.RunVsAggressive";
		// config.bot2Init = "human";

		config.botCommandTimeoutMillis = 200000 * 1000;

		config.game.maxGameRounds = 200;

		config.game.fight = FightMode.CONTINUAL_1_1_A60_D70;

		config.visualize = visual;

		config.replayLog = new File("./replay.log");

		RunGame run = new RunGame(config);
		return run.go();
		
		
	}

	public static void runExternal() {
		BotParser parser = new BotParser(new MCTSBot());
		parser.setLogFile(new File("./MCTSBot.log"));
		parser.run();
	}

	public static void main(String[] args) {
		runInternal(true);

		// JavaBot.exec(new String[]{"conquest.bot.custom.AggressiveBot",
		// "./AggressiveBot.log"});
	}
}
