package test;

import mcts.Strategy;

class BasicStrategy implements Strategy<TState, Integer> {
	public Integer action(TState s) {
		// win if possible
		for (int i = 0 ; i < 9 ; ++i)
			if (s.board[i] == 0 && s.result(i).isDone())
				return i;
		
		// block a win if possible
		TState t = new TState(s.board, 3 - s.player);  // assume other player's turn
		for (int i = 0 ; i < 9 ; ++i)
			if (t.board[i] == 0 && t.result(i).isDone())
				return i;
		
		// move randomly
		return s.randomAction();
	}
}