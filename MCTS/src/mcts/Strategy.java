package mcts;

public interface Strategy<S, A> {
	A action(S state);
}