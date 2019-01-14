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
import conquest.engine.RunGame;
import conquest.game.FightMode;
import conquest.game.world.Continent;
import conquest.game.world.Region;
import mcts.Mcts;

public class MCTSBot extends GameBot {

	Mcts<GameState, Action> mcts;

	@Override
	public ChooseCommand chooseRegion(List<Region> choosable, long timeout) {
		int min = Integer.MAX_VALUE;
		Region best = null;

		for (Region r : choosable) {
			int p = getPreferredContinentPriority(r.continent);
			if (p < min) {
				min = p;
				best = r;
			}
		}

		return new ChooseCommand(best);
	}

	public int getPreferredContinentPriority(Continent continent) {
		switch (continent) {
		case Australia:
			return 1;
		case South_America:
			return 2;
		case North_America:
			return 3;
		case Europe:
			return 4;
		case Africa:
			return 5;
		case Asia:
			return 6;
		default:
			return 7;
		}
	}

	@Override
	public List<PlaceCommand> placeArmies(long timeout) {
		if (mcts == null) {
			WarlightGame game = new WarlightGame(state);
			MCTSGenerator generator = new MCTSGenerator();
			mcts = new Mcts<GameState, Action>(game,generator, new AggressiveBaseStrategy(), 1000, 3);
		}
		return ((PlaceAction) mcts.action(state)).commands;
	}

	@Override
	public List<MoveCommand> moveArmies(long timeout) {
		return ((MoveAction) mcts.action(state)).commands;
	}

	public static void runInternal() {
		Config config = new Config();

		config.bot1Init = "internal:conquest.bot.warlight_hellwig.MCTSBot";

		config.bot2Init = "internal:conquest.bot.custom.AggressiveBot";
		// config.bot2Init = "human";

		config.botCommandTimeoutMillis = 20 * 1000;

		config.game.maxGameRounds = 200;

		config.game.fight = FightMode.CONTINUAL_1_1_A60_D70;

		config.visualize = true;

		config.replayLog = new File("./replay.log");

		RunGame run = new RunGame(config);
		run.go();

		System.exit(0);
	}

	public static void runExternal() {
		BotParser parser = new BotParser(new MCTSBot());
		parser.setLogFile(new File("./MCTSBot.log"));
		parser.run();
	}

	public static void main(String[] args) {
		runInternal();

		// JavaBot.exec(new String[]{"conquest.bot.custom.AggressiveBot",
		// "./AggressiveBot.log"});
	}
}
