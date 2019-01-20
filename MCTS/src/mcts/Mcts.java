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
		this.searchTree = new MCTSTree<S, A>(game, generator);
	}

	@Override
	public A action(S state) {
		Node<S, A> expanded;
		double exploredRating;
		searchTree.updateRoot();
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

	private double defaultPolicy(Node<S, A> expandedNode) {
		S currentState = expandedNode.getState();
		Game<S, A> currGame = game.clone();
		while (!currGame.isDone(currentState)) {
			currGame.apply(currentState, base.action(currentState));
		}
		return currGame.player(searchTree.getRoot().getState()) == game.outcome(currentState) ? 1 : 0;
	}

	public void setTimeLimit(int timeLimit) {
		this.timeLimit = timeLimit;
	}
}
