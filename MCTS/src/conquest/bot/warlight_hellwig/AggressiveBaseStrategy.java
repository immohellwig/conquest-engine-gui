package conquest.bot.warlight_hellwig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import conquest.bot.fight.FightSimulation.FightAttackersResults;
import conquest.bot.fight.FightSimulation.FightDefendersResults;
import conquest.bot.state.Action;
import conquest.bot.state.ChooseCommand;
import conquest.bot.state.GameState;
import conquest.bot.state.MoveAction;
import conquest.bot.state.MoveCommand;
import conquest.bot.state.PlaceAction;
import conquest.bot.state.PlaceCommand;
import conquest.bot.state.PlayerState;
import conquest.bot.state.RegionState;
import conquest.game.Phase;
import conquest.game.world.Continent;
import conquest.game.world.Region;
import conquest.utils.Util;
import mcts.Strategy;

public class AggressiveBaseStrategy implements Strategy<GameState, Action> {
	
	FightAttackersResults aRes;
	FightDefendersResults dRes;

	public AggressiveBaseStrategy() {
		aRes = FightAttackersResults.loadFromFile(Util.file("FightSimulation-Attackers-A200-D200.obj"));
		dRes = FightDefendersResults.loadFromFile(Util.file("FightSimulation-Defenders-A200-D200.obj"));
	}

	@Override
	public Action action(GameState state) {
		if (state.getPhase() == Phase.PLACE_ARMIES) {
			
			PlayerState me = state.players[state.me];
			List<PlaceCommand> result = new ArrayList<PlaceCommand>();
			
			// CLONE REGIONS OWNED BY ME
			List<RegionState> mine = new ArrayList<RegionState>(me.regions.values());
			
			// SORT THEM IN DECREASING ORDER BY SCORE
			Collections.sort(mine, new Comparator<RegionState>() {

				@Override
				public int compare(RegionState o1, RegionState o2) {
					int regionScore1 = getRegionScore(o1, state);
					int regionScore2 = getRegionScore(o2, state);
					return regionScore2 - regionScore1;
				}

			});
			
			// DO NOT ADD SOLDIER TO REGIONS THAT HAVE SCORE 0 (not perspective)
			int i = 0;
			while (i < mine.size() && getRegionScore(mine.get(i), state) > 0) ++i;
			while (i < mine.size()) mine.remove(i);

			// DISTRIBUTE ARMIES
			int armiesLeft = me.placeArmies;
			
			int index = 0;
			
			while (armiesLeft > 0) {
			    int count = Math.min(3, armiesLeft);
				result.add(new PlaceCommand(mine.get(index).region, count));
				armiesLeft -= count;
				++index;
				if (index >= mine.size()) index = 0;
			}
			PlaceAction resultAction = new PlaceAction(result);
			return resultAction;
			
		} else if (state.getPhase() == Phase.ATTACK_TRANSFER) {
			List<MoveCommand> result = new ArrayList<MoveCommand>();
			Collection<RegionState> regions = state.players[state.me].regions.values();
			
			// CAPTURE ALL REGIONS WE CAN
			for (RegionState from : regions) {
				int available = from.armies - 1;  // 1 army must stay behind
				
				for (RegionState to : from.neighbours) {
					// DO NOT ATTACK OWN REGIONS
					if (to.owned(state.me)) continue;
					
					// IF YOU HAVE ENOUGH ARMY TO WIN WITH 70%
					int need = getRequiredSoldiersToConquerRegion(from, to, 0.7);
					
					if (available >= need) {
						// => ATTACK
						result.add(new MoveCommand(from.region, to.region, need));
						available -= need;
					}
				}
			}
			
			// MOVE LEFT OVERS CLOSER TO THE FRONT
			for (RegionState from : regions) {
				if (hasOnlyMyNeighbours(from, state) && from.armies > 1) {
					result.add(moveToFront(from, state));
				}
			}
			MoveAction resultAction = new MoveAction(result);
			return resultAction;
		} else {
			return chooseRegion(state.getPickableRegions(), 1000);
		}
	}
	
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
	
	private int getRegionScore(RegionState o1, GameState state) {
		int result = 0;
		
		for (Region reg : o1.region.getNeighbours()) {
			result += (state.region(reg).owned(0) ? 1 : 0) * 5;
			result += (state.region(reg).owned(state.opp) ? 1 : 0) * 2;
		}
		
		return result;
	}
	
	private boolean hasOnlyMyNeighbours(RegionState from, GameState state) {
		for (RegionState region : from.neighbours) {			
			if (!region.owned(state.me)) return false;
		}
		return true;
	}

	private int getRequiredSoldiersToConquerRegion(RegionState from, RegionState to, double winProbability) {
		int attackers = from.armies - 1;
		int defenders = to.armies;
		
		for (int a = defenders; a <= attackers; ++a) {
			double chance = aRes.getAttackersWinChance(a, defenders);
			if (chance >= winProbability) {
				return a;
			}
		}
		
		return Integer.MAX_VALUE;
	}
		
	private MoveCommand transfer(RegionState from, RegionState to, GameState state) {
		MoveCommand result = new MoveCommand(from.region, to.region, from.armies-1);
		return result;
	}
	
	private MoveCommand moveToFront(RegionState currentRegion, GameState state) {
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
						
						return transfer(currentRegion, state.region(curr.state.region), state);
					}
				}
			}
			i++;
		}
		return null;
	}
	

}
