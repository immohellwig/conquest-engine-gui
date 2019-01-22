package test;

import java.util.Random;

import mcts.Strategy;

class BasicStrategy implements Strategy<TState, Integer> {
	Random rand = new Random(34625388973647L);
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
		return randomAction(s);
	}
	
	int randomAction(TState s) {
		int m = rand.nextInt(s.countEmpty());
		int i = -1;
		for (int j = 0 ; j <= m ; ++j)
			do {
				i += 1;
			} while (s.board[i] != 0);
		
		return i;
	}

//	@Override
//	public void setTimeLimit(int i) {
//		// TODO DEBUG
//		
//	}
}