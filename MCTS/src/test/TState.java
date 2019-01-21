package test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

class TState {
	int[] board = new int[9];
	public int player = 1;
	
	Random rand = new Random();
	
	public TState() { }
	public TState(int[] board, int player) {
		this.board = board; this.player = player;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof TState)) return false;
		
		TState t = (TState) o;
		return player == t.player && Arrays.equals(board, t.board);
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(board) + player;
	}
	
	@Override
	public TState clone() {
		return new TState(board.clone(), player);
	}
	
	List<Integer> actions() {
		List<Integer> r = new ArrayList<Integer>();
		for (int i = 0 ; i < 9 ; ++i)
			if (board[i] == 0)
				r.add(i);
	    return r;
	}
	
	int countEmpty() {
		int count = 0;
		for (int i = 0 ; i < 9 ; ++i)
			if (board[i] == 0)
				++count;
		
		return count;
	}
	
	int randomAction() {
		int m = rand.nextInt(countEmpty());
		int i = -1;
		for (int j = 0 ; j <= m ; ++j)
			do {
				i += 1;
			} while (board[i] != 0);
		
		return i;
	}
	
	void apply(int action) {
		if (board[action] != 0)
			throw new Error("illegal move");
				
		board[action] = player;
		player = 3 - player;
	}
	
	TState result(int action) {
		TState s = clone();
		s.apply(action);
		return s;
	}
	
	int winner(int s, int d) {
		int w = board[s];
		
		return board[s + d] == w && board[s + 2 * d] == w ? w : 0;
	}
	
	static int[] check =   { 0, 1, 2, 0, 3, 6, 0, 2 };
	static int[] check_d = { 3, 3, 3, 1, 1, 1, 4, 2 };

	public int winner() {
		for (int i = 0 ; i < check.length ; ++i) {
			int w = winner(check[i], check_d[i]);
			if (w > 0)
				return w;
		}
		return 0;
	}
	
	public boolean isDone() {
		return (winner() != 0 || countEmpty() == 0);
	}
	
	public double outcome() {
		switch (winner()) {
		case 0: return 0.5;   // draw
		case 1: return 1.0;
		case 2: return 0.0;
		default: throw new Error();
		}
	}
	
	char asChar(int i) {
		switch (i) {
		case 0: return '.';
		case 1: return 'X';
		case 2: return 'O';
		default: throw new Error();
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0 ; i < 3 ; ++i) {
			for (int j = 0 ; j < 3 ; ++j)
				sb.append(String.format("%c ", asChar(board[3 * i + j])));
		    sb.append("\n");
		}
		
		return sb.toString();
	}
}