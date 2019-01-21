package test;

import java.util.ArrayList;
import java.util.List;

import mcts.*;

public class TicTacToe implements Game<TState, Integer> {
	public TState initialState() {	return new TState(); }
	
	public TState clone(TState state) { return state.clone(); }
	
	public int player(TState state) { return state.player; }
	
    public void apply(TState state, Integer action) { state.apply(action); }
	
	public boolean isDone(TState state) { return state.isDone(); }
	
	public double outcome(TState state) { return state.outcome(); }
	
    public static void main(String[] args) {
		TicTacToe game = new TicTacToe();
				
		Strategy<TState, Integer> emm = new Mcts<TState, Integer>(game, new TGenerator(), new BasicStrategy(), 1000, 1); 
		
		Runner.play(game, emm, new TRandomStrategy(), 500);
	}

	@Override
	public List<Possibility<TState>> possibleResults(TState state, Integer action) {
		List<Possibility<TState>> result = new ArrayList<Possibility<TState>>();
		TState newState = state.clone();
		newState.apply(action);
		result.add(new Possibility<TState>(1, newState));
		return result;
	}
}