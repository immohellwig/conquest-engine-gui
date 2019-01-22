package conquest.bot.warlight_hellwig;

import java.util.ArrayList;
import java.util.List;

import conquest.bot.state.Action;
import conquest.bot.state.ChooseCommand;
import conquest.bot.state.GameState;
import conquest.bot.state.MoveAction;
import conquest.bot.state.MoveCommand;
import conquest.bot.state.PlaceAction;
import conquest.bot.state.PlaceCommand;
import conquest.game.Phase;
import mcts.Game;
import mcts.Possibility;

public class WarlightGame implements Game<GameState, Action> {

	private GameState gameState;

	public WarlightGame(GameState state) {
		this.gameState = state;
	}

	@Override
	public GameState initialState() {
		return gameState.clone();
	}

	@Override
	public int player(GameState state) {
		return state.me;
	}

	@Override
	public void apply(GameState state, Action action) {
		if (state.getPhase() == Phase.PLACE_ARMIES) {
			List<PlaceCommand> cmdList = ((PlaceAction) action).commands;
			state.placeArmies(cmdList);
		} else if (state.getPhase() == Phase.ATTACK_TRANSFER) {
			List<MoveCommand> cmdList = ((MoveAction) action).commands;
			state.moveArmies(cmdList);
		} else {
			state.chooseRegion((ChooseCommand) action);
		}
	}

	@Override
	public List<Possibility<GameState>> possibleResults(GameState state, Action action) {
		List<Possibility<GameState>> possibleResults = new ArrayList<Possibility<GameState>>();
		GameState poss = state.clone();
		if (poss.getPhase() == Phase.PLACE_ARMIES) {
			PlaceAction placeAction = (PlaceAction) action;
			poss.placeArmies(placeAction.commands);
		} else if (poss.getPhase() == Phase.ATTACK_TRANSFER) {
			MoveAction moveAction = (MoveAction) action;
			poss.moveArmies(moveAction.commands);
		} else {
			ChooseCommand chooseAction = (ChooseCommand) action;
			poss.chooseRegion(chooseAction);
		}
		possibleResults.add(new Possibility<>(1,poss));
		return possibleResults;
	}

	@Override
	public boolean isDone(GameState state) {
		return state.isDone();
	}

	@Override
	public double outcome(GameState state) {
		switch (state.winningPlayer()) {
		case 0: return 0.5;   // draw
		case 1: return 1.0;
		case 2: return 0.0;
		default: throw new Error();
		}
	}
}
