package test;

import java.util.Random;

import mcts.Strategy;

class TRandomStrategy implements Strategy<TState, Integer> {
	Random rand = new Random();
	
	public Integer action(TState s) {
		return s.randomAction();
	}
}