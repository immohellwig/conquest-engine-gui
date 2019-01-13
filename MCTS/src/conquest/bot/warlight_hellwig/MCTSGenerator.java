package conquest.bot.warlight_hellwig;

import java.util.ArrayList;
import java.util.List;

import conquest.bot.state.Action;
import conquest.bot.state.GameState;
import conquest.bot.state.MoveAction;
import conquest.bot.state.MoveCommand;
import conquest.bot.state.PlaceAction;
import conquest.bot.state.PlaceCommand;
import conquest.bot.state.RegionState;
import conquest.game.world.Region;
import mcts.Generator;

public class MCTSGenerator implements Generator<GameState, Action> {

	@Override
	public List<Action> actions(GameState state) {
		List<Action> moves = new ArrayList<Action>();
		switch (state.getPhase()) {
		case STARTING_REGIONS:
			// return null;
		case PLACE_ARMIES: // TODO: Split-Moves?
			for (RegionState currentRegion : state.regions) {
				if (currentRegion == null)
					continue;
				if (currentRegion.owned(state.me)) {
					for (RegionState neighborState : currentRegion.neighbours) {
						if (!neighborState.owned(state.me)) {
							List<PlaceCommand> cmdList = new ArrayList<PlaceCommand>();
							cmdList.add(new PlaceCommand(currentRegion.region, state.players[state.me].placeArmies));
							moves.add(new PlaceAction(cmdList));
							break;
						}
					}
				}
			}
			return moves;
		case ATTACK_TRANSFER:
			List<List<MoveCommand>> combinationList = new ArrayList<List<MoveCommand>>();

			// Rotate over all owned regions with more than three armees
			for (int i = 1; i < state.regions.length; i++) {
				RegionState currentRegion = state.regions[i];
				if (currentRegion.owned(state.me) && currentRegion.armies > 1) {

					// Check if at border
					boolean isOppNeighbor = false;
					List<Region> mayAttack = new ArrayList<Region>();
					List<Region> mayTransferTo = new ArrayList<Region>();
					for (RegionState neighborRegion : currentRegion.neighbours) {
						if (!neighborRegion.owned(state.me)
								&& currentRegion.armies > 1 + Math.round(neighborRegion.armies * 1.25)) { // CONSTANT WINCHANCE
							if (neighborRegion.owned(state.opp)) {
								isOppNeighbor = true;
							}
							mayAttack.add(neighborRegion.region); // Add potential Attack-target
						} else if (neighborRegion.owned(state.me)) {
							for (RegionState neighborOfNeighbor : neighborRegion.neighbours) {
								if (!neighborOfNeighbor.owned(state.me)) {
									mayTransferTo.add(neighborRegion.region); // Add potential Troop movement within
																				// territory
									break;
								}
							}
						}
					}
					List<MoveCommand> attackCommands = new ArrayList<MoveCommand>();
					List<MoveCommand> transferCommands = new ArrayList<MoveCommand>();
					int enemyNeighbors = 0;
					if (currentRegion.armies > 3) {
						enemyNeighbors = mayAttack.size();
					}
					if (enemyNeighbors > 1) {
						if (isOppNeighbor) {
							for (int j = 0; j < mayAttack.size(); j++) {
								attackCommands.add(new MoveCommand(currentRegion.region, mayAttack.get(j),
										Math.round((currentRegion.armies - 1) / 2)));
							}
						} else {
							for (int j = 0; j < mayAttack.size(); j++) {
								attackCommands.add(new MoveCommand(currentRegion.region, mayAttack.get(j),
										Math.round((currentRegion.armies - 1))));
							}
						}
					} else if (enemyNeighbors == 1) {
						attackCommands
								.add(new MoveCommand(currentRegion.region, mayAttack.get(0), currentRegion.armies - 1));
					} else if (enemyNeighbors == 0) {
						if (!mayTransferTo.isEmpty()) {
							for (Region currentOption : mayTransferTo) {
								transferCommands.add(
										new MoveCommand(currentRegion.region, currentOption, currentRegion.armies - 1));
							}
						} else { // TODO: Find shortest way to border
							for (RegionState neighbor : currentRegion.neighbours) {
								transferCommands.add(new MoveCommand(currentRegion.region, neighbor.region,
										currentRegion.armies - 1));
							}
						}
					}
					if (attackCommands.isEmpty()) {
						combinationList.add(transferCommands);
					} else {
						attackCommands.add(null);
						combinationList.add(attackCommands);
					}
				}
			}
			return combinations(combinationList);
		default:
			return null;
		}

	}

	private List<Action> combinations(List<List<MoveCommand>> combinationList) {
		List<List<MoveCommand>> returnList = new ArrayList<List<MoveCommand>>();
		recursiveCombinations(combinationList, returnList, new ArrayList<MoveCommand>(), combinationList.size());
		List<Action> actionList = new ArrayList<Action>();
		for (List<MoveCommand> currentCombination : returnList) {
			actionList.add(new MoveAction(currentCombination));
		}
		return actionList;
	}

	private void recursiveCombinations(List<List<MoveCommand>> combinationList, List<List<MoveCommand>> returnList,
			List<MoveCommand> currentCombination, int depth) {

		if (depth == 0 || currentCombination.size() >= 4) { // TODO: Better Way?
			if (!currentCombination.isEmpty()) {
				List<MoveCommand> copyCurrent = new ArrayList<MoveCommand>(currentCombination);
				returnList.add(copyCurrent);
			}
			return;
		}
		for (MoveCommand currentElement : combinationList.get(depth - 1)) {
			if (currentElement != null)
				currentCombination.add(currentElement);
			recursiveCombinations(combinationList, returnList, currentCombination, depth - 1);
			currentCombination.remove(currentElement);
		}
	}
}
