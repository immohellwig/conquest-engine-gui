package mcts;

public interface Strategy<S, A> {
	A action(S state);
//	void setTimeLimit(int i); // TODO DEBUG
}