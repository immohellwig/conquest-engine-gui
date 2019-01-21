package mcts;

import java.util.List;

public interface Game<S, A> {
	S initialState();

	int player(S state); // which player moves next: 1 (maximizing) or 2 (minimizing)

	void apply(S state, A action); // stochastic result of action

	List<Possibility<S>> possibleResults(S state, A action); // some or all possible results, ordered by probability

	boolean isDone(S state); // true if game has finished

	double outcome(S state); // 1.0 = player 1 wins, 0.5 = draw, 0.0 = player 2 wins

//	Game<S, A> clone();

//	double evaluate(S state); // expected outcome from this state (for expectiminimax only)
}