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
	
	public void updateRoot(final S state) {
		S init = state;
		List<A> moveCandidates = generator.actions(init);
		root = new MCTSNode<S, A>(init, moveCandidates, game.player(state));
	}

	public MCTSNode<S, A> treePolicy() {
		MCTSNode<S, A> currentNode = root;
		double expConst = 1 / Math.sqrt(2); // paper
		while (!game.isDone(currentNode.getState())) {
			if (currentNode.isNotFullyExpanded()) {
				return expand(currentNode);
			} else {
				currentNode = currentNode.getBestRatedChild(expConst, game.player(root.getState()));
			}
		}
		return currentNode != root ? currentNode : null;
	}
	
	private MCTSNode<S, A> expand(final MCTSNode<S, A> currentNode) {
		A nextAction = currentNode.popRandomAction();
		S newState = game.possibleResults(currentNode.getState(), nextAction).get(0).state;
		MCTSNode<S, A> expanded = new MCTSNode<S, A>(nextAction, newState, generator.actions(newState), currentNode, game.player(newState));
		return expanded;
	}

	public void propagateBack(final MCTSNode<S, A> expanded, double exploredRating) {
		MCTSNode<S, A> currentNode = expanded;
		while (currentNode != null) {
			currentNode.addRating(exploredRating);
			currentNode = currentNode.getFather();
		}

	}
}
