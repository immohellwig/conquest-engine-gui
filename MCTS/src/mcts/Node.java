package mcts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

class Node<S, A> {

	/**
	 * Father: Representing former state
	 */

	final private Node<S, A> father;

	/**
	 * State of the Node
	 */

	final private S state;

	/**
	 * Children: Representing explored/expanded possibilities
	 */

	final List<Node<S, A>> children;

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

	private double numerator;

	/**
	 * Denominator of Rating
	 */

	private double denominator;
	
	/**
	 * Root constructor, initializes rating with 0
	 */

	Node(S state, List<A> possibleActions) {
		this(null, state, possibleActions, null);
	}

	Node(A lastAction, S state, List<A> possibleActions, Node<S, A> father) {
		numerator = 0;
		denominator = 0;
		this.state = state;
		this.possilbleActions = possibleActions;
		this.father = father;
		this.children = new ArrayList<Node<S, A>>();
		this.lastAction = lastAction;
		if (father != null)
			this.father.addChild(this);
	}

	void addChild(Node<S, A> node) {
		children.add(node);
	}

	Node<S, A> getBestRatedChild(double exlorationConstant) { // TODO Implement ExplorationConstant
		Iterator<Node<S, A>> iter = children.iterator();
		Node<S, A> bestRated;
		if (iter.hasNext()) {
			bestRated = iter.next();
		} else {
			System.err.println("Root has no Children!");
			return null;
		}
		Node<S, A> current;
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

	Node<S, A> getFather() {
		return father;
	}

	double getRating() {
		return numerator / denominator;
	}
	
	double getExploRating(double eC, double numeratorFather) {
		return ((1 - numerator) / denominator) + eC * Math.sqrt((2 * Math.log(numeratorFather))/numerator);
	}

	S getState() {
		return state;
	}
	
	@Override
	public String toString() {
		return Double.toString(getRating()) + lastAction;
	}
}
