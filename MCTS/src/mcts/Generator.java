package mcts;

import java.util.List;

public interface Generator<S, A> {
	  List<A> actions(S state);  // actions to try in this state
}