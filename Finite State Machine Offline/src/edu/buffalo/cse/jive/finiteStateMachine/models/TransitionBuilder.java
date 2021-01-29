package edu.buffalo.cse.jive.finiteStateMachine.models;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import edu.buffalo.cse.jive.finiteStateMachine.FSMConstants;
import edu.buffalo.cse.jive.finiteStateMachine.models.State.Status;
import edu.buffalo.cse.jive.finiteStateMachine.util.Pair;
import edu.buffalo.cse.jive.finiteStateMachine.util.TemporaryDataTransporter;

/**
 * @author Shashank Raghunath
 * @email sraghuna@buffalo.edu
 * 
 * @author Amlan Gupta
 * @email amlangup@buffalo.edu
 * 
 * @author Venugopal CG (Added transition count)
 * @email vchintha@buffalo.edu
 *
 */
/**
 * Given the adjacency list and the root state, builds the String required for
 * SVG generator.
 *
 */
public class TransitionBuilder {

	private StringBuilder transitions;
	private LinkedHashMap<String,Integer> transitionsCount;
	private State rootState;
	private Map<State, Set<State>> states;
	private boolean isTransitionCountEnabled;

	public TransitionBuilder(State rootState, Map<State, Set<State>> states, LinkedHashMap<String,Integer> transitionsCount, boolean isTransitionCountEnabled) {
		transitions = new StringBuilder();
		transitions.append(FSMConstants.START_UML+FSMConstants.NEW_LINE);
		this.rootState = rootState;
		this.transitionsCount = transitionsCount;
		this.states = states;
		this.isTransitionCountEnabled = isTransitionCountEnabled;
	}

	private void addInitialState(State state, Status status) {
		String transition = MessageFormat.format(FSMConstants.START_STATE, state.toString());
		this.transitionsCount.put(transition, 1);
		transition = checkAndAddTransitionCount(transition);
		if (status.equals(Status.VALID) || status.equals(Status.VALID_A))
			this.transitions.append(transition);
		else if (status.equals(Status.MARKED))
			addColorTransition(transition, FSMConstants.LIME_GREEN_COLOR);
		else if (status.equals(Status.VALID_A))
			addColorTransition(transition, FSMConstants.LIGHT_SKY_BLUE_COLOR);
		else 
			addColorTransition(transition, FSMConstants.RED_COLOR);
		addNewLine();
	}

	public String getTransitions() {
		return new StringBuilder(transitions).append(FSMConstants.END_UML+FSMConstants.NEW_LINE).toString();
	}

	private void addTransition(State state1, State state2, Status status) {
		String transition = MessageFormat.format(FSMConstants.TRANSITION, state1.toString(), state2.toString());
		transition = checkAndAddTransitionCount(transition);
		if (status.equals(Status.INVALID))
			addColorTransition(transition, FSMConstants.RED_COLOR);
		else if (status.equals(Status.VALID_A))
			addColorTransition(transition, FSMConstants.LIGHT_SKY_BLUE_COLOR);
		else if (TemporaryDataTransporter.F_success_states.contains(state2))
			addColorTransition(transition, FSMConstants.LIME_GREEN_COLOR);
		else
			this.transitions.append(transition);
		addNewLine();
	}
	
	private void addColorTransition(String transition, String color) {
		this.transitions.append(transition + FSMConstants.SPACE_HASH + color);
	}
	
	private String checkAndAddTransitionCount(String transition) {
		if(!isTransitionCountEnabled)
			return transition;
		return transition.replaceAll(FSMConstants.TRANSITION_ARROW, 
				MessageFormat.format(FSMConstants.TRANSITION_COUNT,this.transitionsCount.get(transition)));
	}
	
	private void addColorTransitionWithArrowBetweenSameStates(State state1, State state2, String backgroundColor, String arrowColor) {	
		Pair<State, State> pair = new Pair<State, State>(state1, state2);
		String transition = MessageFormat.format(FSMConstants.TRANSITION, state1.toString(), state2.toString());
		if(TemporaryDataTransporter.getPath().contains(pair)) {
			String s = "-[#"+arrowColor+"]->";
			if(isTransitionCountEnabled)
				s += " ["+ this.transitionsCount.get(transition) +"]";
			transition = transition.replaceAll(FSMConstants.TRANSITION_ARROW, s);
		} else if(isTransitionCountEnabled) {
			transition = transition.replaceAll(FSMConstants.TRANSITION_ARROW, 
					MessageFormat.format(FSMConstants.TRANSITION_COUNT,this.transitionsCount.get(transition)));
		}		
		if(backgroundColor.length()>0)
			transition += FSMConstants.SPACE_HASH + backgroundColor;
		this.transitions.append(transition);
		addNewLine();	
	}

	private void addNewLine() {
		this.transitions.append(FSMConstants.NEW_LINE);
	}

	public void build() {
		addInitialState(rootState, rootState.getStatus());
		//buildTransitions(null, rootState, new HashSet<Pair<State, State>>());
		buildTransitions(rootState);
	}
	
	private void buildTransitions(State rootState) {	
		Queue<State> toBeVisited = new LinkedList<State>();
		Set<Pair<State, State>> traversedPath =  new HashSet<Pair<State, State>>();
		Set<State> visited = new HashSet<State>();
		toBeVisited.add(rootState);
		while(!toBeVisited.isEmpty()){
			State curr = toBeVisited.poll();
			for (State next : states.get(curr)){
				if (traversedPath.add(new Pair<State, State>(curr, next))) {
					if(next.getStatus().equals(Status.MARKED) && TemporaryDataTransporter.shouldHighlight)
						addColorTransitionWithArrowBetweenSameStates(curr,next, FSMConstants.LIME_GREEN_COLOR, FSMConstants.GREEN_COLOR);
					else 
						addTransition(curr, next, next.getStatus());
					if(visited.add(next))toBeVisited.add(next);
				}	
			}	
		}
		System.out.println(transitions.toString());
	}

}
