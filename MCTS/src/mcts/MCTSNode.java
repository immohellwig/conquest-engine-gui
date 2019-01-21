package mcts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

class MCTSNode<S, A> {

	/**
	 * Father: Representing former state
	 */

	final private MCTSNode<S, A> father;

	/**
	 * State of the Node
	 */

	final private S state;

	/**
	 * Children: Representing explored/expanded possibilities
	 */

	final private List<MCTSNode<S, A>> children;

	/**
	 * Unexplored actions
	 */

	final private List<A> possilbleActions;

	/**
	 * Move, that led to the current state
	 */

	final private A lastAction;

	/**
	 * Numerator of Rating
	 */

	private double numerator = 0;

	/**
	 * Denominator of Rating
	 */

	private double denominator = 0;
	
	/**
	 * Root constructor, initializes rating with 0
	 */

	MCTSNode(S state, List<A> possibleActions) {
		this(null, state, possibleActions, null);
	}

	MCTSNode(A lastAction, S state, List<A> possibleActions, MCTSNode<S, A> father) {
		this.state = state;
		this.possilbleActions = possibleActions;
		this.father = father;
		this.children = new ArrayList<MCTSNode<S, A>>();
		this.lastAction = lastAction;
		if (father != null)
			this.father.addChild(this);
	}

	void addChild(MCTSNode<S, A> node) {
		children.add(node);
	}

	MCTSNode<S, A> getBestRatedChild(double exlorationConstant) { // TODO Implement ExplorationConstant
		Iterator<MCTSNode<S, A>> iter = children.iterator();
		MCTSNode<S, A> bestRated;
		if (iter.hasNext()) {
			bestRated = iter.next();
		} else {
			System.err.println("Root has no Children!");
			return null;
		}
		MCTSNode<S, A> current;
		while (iter.hasNext()) {
			current = iter.next();
			if (bestRated.getExploRating(exlorationConstant, numerator) < current.getExploRating(exlorationConstant, numerator)) {
				bestRated = current;
			}
		}
		return bestRated;
	}

	A popRandomAction() {
		Random rand = new Random();
		int index = rand.nextInt(possilbleActions.size());
		A selectedAction = possilbleActions.get(index);
		possilbleActions.remove(index);
		return selectedAction;
	}

	void addRating(double newRating) {
		numerator += newRating;
		denominator++;
	}

	boolean hasChildren() {
		return !children.isEmpty();
	}

	boolean isNotFullyExpanded() {
		return !possilbleActions.isEmpty();
	}

	A getAction() {
		return lastAction;
	}

	MCTSNode<S, A> getFather() {
		return father;
	}

	double getRating() {
		return numerator / denominator;
	}
	
	double getExploRating(double eC, double numeratorFather) {
		if (numerator != 0)
			return ((1 - numerator) / denominator) + eC * Math.sqrt((2 * Math.log(numeratorFather))/numerator);
		else
			return ((1 - numerator) / denominator);
	}

	S getState() {
		return state;
	}
	
	@Override
	public String toString() {
		return Double.toString(getExploRating(0,0));
	}
}
