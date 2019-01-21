package test;

import java.util.List;

import mcts.Generator;
import mcts.Possibility;

class TGenerator implements Generator<TState, Integer> {
    public List<Integer> actions(TState s) { return s.actions(); }

	public List<Possibility<TState>> possibleResults(TState state, Integer action) {
		return List.of(new Possibility<TState>(1.0, state.result(action)));
	}
}