package mcts;

public class Mcts<S, A> implements Strategy<S, A> {
	private int timeLimit;

//	final private int determinizations; // TODO: Implement!

	final private MCTSTree<S, A> searchTree;

	final private Game<S, A> game;

	final private Strategy<S, A> base;

	public Mcts(Game<S, A> game, Generator<S, A> generator, Strategy<S, A> base, int determinizations, int timeLimit) {
		this.timeLimit = timeLimit;
//		this.determinizations = determinizations;
		this.game = game;
		this.base = base;
		this.searchTree = new MCTSTree<S, A>(game, generator, game.initialState());
	}

	@Override
	public A action(S state) {
		MCTSNode<S, A> expanded;
		double exploredRating;
		searchTree.updateRoot(state);
		int counter = 0;

		long time = System.currentTimeMillis();
		long endingTime = time + timeLimit * 1000;
		while (System.currentTimeMillis() < endingTime) {
			expanded = searchTree.treePolicy();
			exploredRating = defaultPolicy(expanded);
			searchTree.propagateBack(expanded, exploredRating);
			counter++;
		}
		try {
			A result = searchTree.getRoot().getBestRatedChild(0).getAction();
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
			return game.player(searchTree.getRoot().getState()) == game.outcome(currentState) ? 1 : 0;
		} else {
			return game.player(searchTree.getRoot().getState()) == game.outcome(expandedNode.getState()) ? 1 : 0;
		}
	}

	public void setTimeLimit(int timeLimit) {
		this.timeLimit = timeLimit;
	}
}
