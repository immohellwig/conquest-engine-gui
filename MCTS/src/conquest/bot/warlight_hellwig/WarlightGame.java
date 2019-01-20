package conquest.bot.warlight_hellwig;

import java.util.ArrayList;
import java.util.List;

import conquest.bot.fight.FightSimulation.FightAttackersResults;
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
	public GameState apply(GameState state, Action action) {
		GameState alteredState = state;
		if (alteredState.getPhase() == Phase.PLACE_ARMIES) {
			List<PlaceCommand> cmdList = ((PlaceAction) action).commands;
			alteredState.placeArmies(cmdList);
			return alteredState;
		} else if (alteredState.getPhase() == Phase.ATTACK_TRANSFER) {
			List<MoveCommand> cmdList = ((MoveAction) action).commands;
			alteredState.moveArmies(cmdList);
			return alteredState;
//			return getMostProbableOutcome(alteredState, cmdList);
		} else {
			alteredState.chooseRegion((ChooseCommand) action);
			return alteredState;
		}
	}
	
//	private GameState getMostProbableOutcome(GameState alteredState, List<MoveCommand> cmdList) {
//		for (MoveCommand currCommand : cmdList) {
//			int attSurvivers = FightAttackersResults
//			int defSurvivers;
//		}
//		return alteredState;
//	}

	/**
	 * Not Used
	 */

	@Override
	public List<Possibility<GameState>> possibleResults(GameState state, Action action) {
		List<Possibility<GameState>> possibleResults = new ArrayList<Possibility<GameState>>();
		GameState poss = state.clone();
		if (gameState.getPhase() == Phase.PLACE_ARMIES) {
			PlaceAction placeAction = (PlaceAction) action;
			Possibility<GameState> toAdd = new Possibility<GameState>();
			poss.placeArmies(placeAction.commands);
			toAdd.prob = 1;
			toAdd.state = poss;
			possibleResults.add(toAdd);
		} else if (gameState.getPhase() == Phase.ATTACK_TRANSFER) { 
			MoveAction moveAction = (MoveAction) action;
			Possibility<GameState> toAdd = new Possibility<>();
			poss.moveArmies(moveAction.commands);
			toAdd.prob = 1;
			toAdd.state = poss;
			possibleResults.add(toAdd);
		}
		return possibleResults;
	}

	@Override
	public boolean isDone(GameState state) {
		return state.isDone();
	}

	@Override
	public double outcome(GameState state) {
		return state.winningPlayer();
	}

	@Override
	public Game<GameState, Action> clone() {
		return new WarlightGame(gameState);
	}
}
