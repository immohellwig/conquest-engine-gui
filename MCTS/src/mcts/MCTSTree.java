package mcts;

import java.util.List;

class MCTSTree<S, A> {
	private MCTSNode<S, A> root;
	final private Game<S, A> game;
	final private Generator<S, A> generator;

	public MCTSTree(Game<S, A> game, Generator<S, A> generator, S state) {
		this.game = game;
		this.generator = generator;
		updateRoot(state);
	}

	public MCTSNode<S, A> getRoot() {
		return root;
	}
	
	public boolean updateRoot(final S state) {
		S init = state;
		List<A> moveCandidates = generator.actions(init);
		root = new MCTSNode<S, A>(init, moveCandidates);
		return true;
	}

	public MCTSNode<S, A> treePolicy() {
		MCTSNode<S, A> currentNode = getRoot();
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
	
	private MCTSNode<S, A> expand(final MCTSNode<S, A> currentNode) { // TODO: randomize?
		A nextAction = currentNode.popRandomAction();
		S newState = game.possibleResults(currentNode.getState(), nextAction).get(0).state;
		MCTSNode<S, A> expanded = new MCTSNode<S, A>(nextAction, newState, generator.actions(newState), currentNode);
		return expanded;
	}

	public void propagateBack(final MCTSNode<S, A> expanded, double exploredRating) {
		MCTSNode<S, A> currentNode = expanded;
		boolean playerColor;
		while (currentNode != null) {
			playerColor = game.player(currentNode.getState()) == game.player(root.getState());
			currentNode.addRating(playerColor ? exploredRating : 1 - exploredRating);
			currentNode = currentNode.getFather();
		}

	}
}
