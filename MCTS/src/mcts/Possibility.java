package mcts;

public class Possibility<S> {
	public Possibility(double d, S result) {
		prob = d;
		state = result;
	}
	public double prob; // probability from 0..1
	public S state;
}