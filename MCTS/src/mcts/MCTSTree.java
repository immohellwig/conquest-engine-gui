package mcts;

import java.util.List;

class MCTSTree<S, A> {
	private Node<S, A> root;
	final private Game<S, A> game;
	final private Generator<S, A> generator;

	public MCTSTree(Game<S, A> game, Generator<S, A> generator) {
		this.game = game;
		this.generator = generator;
		updateRoot();
	}

	public Node<S, A> getRoot() {
		return root;
	}

	public Node<S, A> treePolicy() {
		Node<S, A> currentNode = getRoot();
		double expConst = 1 / Math.sqrt(2); // paper
		while (!game.isDone(currentNode.getState())) {
			if (currentNode.isNotFullyExpanded()) {
				return expand(currentNode);
			} else {
				currentNode = currentNode.getBestRatedChild(expConst);
			}
		}
		return currentNode;
	}

	public boolean updateRoot() {
		S init = game.initialState();
		List<A> moveCandidates = generator.actions(init);
		root = new Node<S, A>(init, moveCandidates);
		return true;
	}

	public void propagateBack(final Node<S, A> expanded, double exploredRating) {
		Node<S, A> currentNode = expanded;
		boolean playerColor;
		while (currentNode != null) {
			playerColor = game.player(currentNode.getState()) == game.player(root.getState());
			currentNode.addRating(playerColor ? exploredRating : 1 - exploredRating);
			currentNode = currentNode.getFather();
		}

	}

	private Node<S, A> expand(Node<S, A> currentNode) { // TODO: randomize?
		A nextAction = currentNode.popRandomAction();

//		Stochastic Approach
		S newState = game.clone().apply(currentNode.getState(), nextAction);

		Node<S, A> expanded = new Node<S, A>(nextAction, newState, generator.actions(newState), currentNode);
		return expanded;
	}
}
