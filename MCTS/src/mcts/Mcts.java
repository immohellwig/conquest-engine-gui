package mcts;

public class Mcts<S, A> implements Strategy<S, A> {
	private int timeLimit;

	final private MCTSTree<S, A> searchTree;

	final private Game<S, A> game;

	final private Strategy<S, A> base;

	public Mcts(Game<S, A> game, Generator<S, A> generator, Strategy<S, A> base, int determinizations, int timeLimit) {
		this.timeLimit = timeLimit;
		this.game = game;
		this.base = base;
		this.searchTree = new MCTSTree<S, A>(game, generator, game.initialState());
	}

	@Override
	public A action(S state) {
		MCTSNode<S, A> expanded;
		double exploredRating;
		searchTree.updateRoot(state);
		double counter = 0;

		long time = System.currentTimeMillis();
		long endingTime = time + timeLimit;
		while (System.currentTimeMillis() < endingTime) {
//			if (System.currentTimeMillis() > time + 1000)  {// TODO DEBUG
//				System.out.println("DEBUG");
//			}
			expanded = searchTree.treePolicy();
			if (expanded == null)
				break;
			exploredRating = defaultPolicy(expanded);
			searchTree.propagateBack(expanded, exploredRating);
			counter++;
		}
		try {
			System.out.println("Speed: " + counter / ((double) timeLimit / 1000) + " N/s");
			A result = searchTree.getRoot().getBestRatedChild(0,game.player(state)).getAction();
			return result;
		} catch (Exception e) {
			System.err.println("Expanded: " + counter);
			return null;
		}
	}

	private double defaultPolicy(MCTSNode<S, A> expandedNode) {
		if (!game.isDone(expandedNode.getState())) {
			S currentState = game.possibleResults(expandedNode.getState(), base.action(expandedNode.getState()))
					.get(0).state;
			while (!game.isDone(currentState)) {
				game.apply(currentState, base.action(currentState));
			}
			return getRating(currentState);
		} else {
			return getRating(expandedNode.getState());
		}
	}
	
	private double getRating(S currentState) {
		double result = game.outcome(currentState);
		int me = game.player(searchTree.getRoot().getState());
		if (result == 0.0)
			return 2 == me ? 1 : 0;
		else if (result == 0.5)
			return 0.5; // draw
		else if (result == 1.0)
			return 1 == me ? 1 : 0;
		System.err.println("Invalid outcome");
		return -1;
	}
	
	public void setTimeLimit(int timeLimit) {
		this.timeLimit = timeLimit;
	}
}
