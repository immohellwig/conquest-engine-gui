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
import conquest.bot.state.RegionState;
import conquest.game.world.Continent;
import conquest.game.world.Region;
import conquest.utils.Util;
import mcts.Generator;

public class MCTSGenerator implements Generator<GameState, Action> {

	@Override
	public List<Action> actions(GameState state) {
		List<Action> moves = new ArrayList<Action>();
		switch (state.getPhase()) {
		case STARTING_REGIONS:
			return chooseRegion(state.getPickableRegions(), 1000);
		case PLACE_ARMIES: // TODO: Split-Moves?
			for (RegionState currentRegion : state.regions) {
				if (currentRegion == null)
					continue;
				if (currentRegion.owned(state.me)) {
					for (RegionState neighborState : currentRegion.neighbours) {
						if (!neighborState.owned(state.me)) {
							List<PlaceCommand> cmdList = new ArrayList<PlaceCommand>();
							cmdList.add(new PlaceCommand(currentRegion.region,
									state.players[state.me].placeArmies < 6 ? 5 : state.players[state.me].placeArmies));
							moves.add(new PlaceAction(cmdList));
							break;
						}
					}
				}
			}
			return moves;
		case ATTACK_TRANSFER:
			List<List<MoveCommand>> attackList = new ArrayList<List<MoveCommand>>();
			List<MoveCommand> transferList = new ArrayList<MoveCommand>();
			FightAttackersResults res = FightAttackersResults
					.loadFromFile(Util.file("FightSimulation-Attackers-A200-D200.obj"));
			// Rotate over all owned regions with more than one armee

			for (RegionState currentRegion : state.regions) {
				if (currentRegion == null)
					continue;
				if (currentRegion.owned(state.me) && currentRegion.armies > 1) {
					int enemyNeighbors = 0;
					// Check if at border
					boolean regionHasOppNeighbor = false;
					List<Region> mayAttack = new ArrayList<Region>();
					Region mayTransferTo = null;
					for (RegionState neighborRegion : currentRegion.neighbours) {
						if (!neighborRegion.owned(state.me)
								&& res.getAttackersWinChance(currentRegion.armies, neighborRegion.armies) > 0.75) {
							// WINCHANCE
							if (neighborRegion.owned(state.opp)) {
								regionHasOppNeighbor = true;
							}
							mayAttack.add(neighborRegion.region); // Add potential Attack-target
							enemyNeighbors++;
						} else if (!neighborRegion.owned(state.me)) {
							enemyNeighbors++;
						}
					}

					if (mayAttack.isEmpty() && enemyNeighbors == 0) {
						mayTransferTo = pathToBorder(state, currentRegion);
					}

					List<MoveCommand> attackCommands = new ArrayList<MoveCommand>();
					List<MoveCommand> transferCommands = new ArrayList<MoveCommand>();
					if (currentRegion.armies <= 3 || mayAttack.isEmpty()) {
						enemyNeighbors = 0; // TODO Refactor
					}
					if (enemyNeighbors > 1) {
						if (regionHasOppNeighbor) {
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
					} else if (enemyNeighbors == 0 && mayTransferTo != null) {
						transferCommands
								.add(new MoveCommand(currentRegion.region, mayTransferTo, currentRegion.armies - 1));
					}
					if (!attackCommands.isEmpty()) {
						attackCommands.add(null);
						attackList.add(attackCommands);
					} else if (!transferCommands.isEmpty()){
						transferList.addAll(transferCommands);
					} else {
						
					}
				}
			}
			List<Action> finalList = combinations(attackList, transferList);
			if (finalList.isEmpty()) { // No Options? Random Move!
				for (RegionState currentRegion : state.regions) {
					if (currentRegion == null)
						continue;
					if (currentRegion.owned(state.me) && currentRegion.armies > 1) {
						for (RegionState neighbor : currentRegion.neighbours) {
							transferList.add(new MoveCommand(currentRegion, neighbor, currentRegion.armies - 1));
							finalList.add(new MoveAction(transferList));
						}
						break;
					}
				}
			}
			return finalList;
		default:
			System.out.println("DefaultCase in Generator!");
			return null;
		}

	}

	private Region pathToBorder(GameState state, RegionState currentRegion) {
		class Node {
			RegionState state;
			Node previous;

			Node(RegionState state) {
				this.state = state;
			}
		}
		List<Node> checked = new ArrayList<Node>();
		List<RegionState> unchecked = new ArrayList<RegionState>();
		checked.add(new Node(currentRegion));
		for (RegionState r : state.regions) {
			if (r != currentRegion)
				unchecked.add(r);
		}
		int i = 0;
		while (i < checked.size()) {
			for (RegionState current : checked.get(i).state.neighbours) {
				if (unchecked.contains(current)) {
					Node curr = new Node(current);
					checked.add(curr);
					unchecked.remove(current);
					curr.previous = checked.get(i);
					if (!curr.state.owned(state.me)) {
						while (curr.previous.state != currentRegion) {
							curr = curr.previous;
						}
						return curr.state.region;
					}
				}
			}
			i++;
		}
		return null;
	}

	private List<Action> combinations(List<List<MoveCommand>> attackList, List<MoveCommand> transferList) {
		List<List<MoveCommand>> returnList = new ArrayList<List<MoveCommand>>();
		recursiveCombinations(attackList, returnList, new ArrayList<MoveCommand>(), attackList.size());
		List<Action> actionList = new ArrayList<Action>();
		for (List<MoveCommand> currentCombination : returnList) {
			currentCombination.addAll(transferList);
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

	private List<Action> chooseRegion(List<Region> choosable, long timeout) {
		int min = Integer.MAX_VALUE;
		List<Action> returnList = new ArrayList<Action>();

		for (Region r : choosable) {
			int p = getPreferredContinentPriority(r.continent);
			if (p < min) {
				returnList.clear();
				min = p;
				returnList.add(new ChooseCommand(r));
			} else if (p == min) {
				returnList.add(new ChooseCommand(r));
			}
		}

		return returnList;
	}

	private int getPreferredContinentPriority(Continent continent) {
		switch (continent) {
		case Australia:
			return 3;
		case South_America:
			return 1;
		case North_America:
			return 4;
		case Europe:
			return 5;
		case Africa:
			return 2;
		case Asia:
			return 6;
		default:
			return 7;
		}
	}
}
