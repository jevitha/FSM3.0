package edu.buffalo.cse.jive.finiteStateMachine.models;

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

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
	private int count;
	private boolean isTransitionCountEnabled;
	List<State> seqStates; // Sequence of original states

	public TransitionBuilder(State rootState, Map<State, Set<State>> states, boolean isTransitionCountEnabled, List<State> seqStates, int count) {
		this.transitions = new StringBuilder();
		this.transitions.append(FSMConstants.START_UML+FSMConstants.NEW_LINE);
		this.rootState = rootState;
		this.count = count;
		this.isTransitionCountEnabled = isTransitionCountEnabled;
		this.seqStates = seqStates;
		this.transitionsCount = new LinkedHashMap<String,Integer>();
	}

	private void addInitialState(State state, Status status) {
		StringBuilder transition = new StringBuilder(
				MessageFormat.format(FSMConstants.START_STATE, state.toString()));
		/*this.transitionsCount.put(transition, 1);
		transition = checkAndAddTransitionCount(transition);
		if (status.equals(Status.VALID) || status.equals(Status.VALID_A))
			this.transitions.append(transition);*/
		if (status.equals(Status.MARKED))
			addColorTransition(transition, FSMConstants.LIME_GREEN_COLOR);
		else if (status.equals(Status.VALID_A))//check the if conditions later
			addColorTransition(transition, FSMConstants.LIGHT_SKY_BLUE_COLOR);
		else 
			addColorTransition(transition, FSMConstants.GREEN_COLOR);
		this.transitions.append(transition.toString());
		addNewLine();
	}

	public String getTransitions() {
		return new StringBuilder(transitions).append(FSMConstants.END_UML+FSMConstants.NEW_LINE).toString();
	}

	private String addTransition(State state1, State state2, Status status) {
		StringBuilder transition = new StringBuilder(
				MessageFormat.format(FSMConstants.TRANSITION, state1.toString(), state2.toString()));
		if (status.equals(Status.INVALID))
			addColorTransition(transition, FSMConstants.RED_COLOR);
		else if (status.equals(Status.VALID_A))
			addColorTransition(transition, FSMConstants.LIGHT_SKY_BLUE_COLOR);
		else if(status.equals(Status.INVALID_ABSTRACTION))
			addColorTransition(transition, FSMConstants.ORANGE_COLOR);
		else if (TemporaryDataTransporter.F_success_states.contains(state2))
			addColorTransition(transition, FSMConstants.LIME_GREEN_COLOR);
		return transition.toString();
	}
	
	private void addColorTransition(StringBuilder transition, String color) {
		transition.append(FSMConstants.SPACE_HASH + color);
	}
	
	private String addColorTransitionWithArrowBetweenSameStates(State state1, State state2, String backgroundColor, String arrowColor) {	
		Pair<State, State> pair = new Pair<State, State>(state1, state2);
		String transition = MessageFormat.format(FSMConstants.TRANSITION, state1.toString(), state2.toString());
		if(TemporaryDataTransporter.getPath().contains(pair)) {
			String s = "-[#"+arrowColor+"]->";
			transition = transition.replaceAll(FSMConstants.TRANSITION_ARROW, s);
		}
		if(backgroundColor.length()>0)
			transition += FSMConstants.SPACE_HASH + backgroundColor;
		return transition;
	}

	private void addNewLine() {
		this.transitions.append(FSMConstants.NEW_LINE);
	}

	public void build() {
		if(rootState != null) {
		addInitialState(rootState, rootState.getStatus());
		buildTransitions(seqStates);
		}
		else {
			System.err.println("Root State is null. "
					+ "This might be caused when there are no JIVE events available."
					+ "Please select JIVE events file or listen on port for events. ");
			
			return;
			
		}
	}
	
	private void addTransitionCount(String transition, int count) {
		if(isTransitionCountEnabled) {
			this.transitions.append(transition.replaceAll(FSMConstants.TRANSITION_ARROW, 
					MessageFormat.format(FSMConstants.TRANSITION_COUNT, count)));
		} else {
			this.transitions.append(transition);
		}
		addNewLine();
	}
	
	public void printTransitions() {
		transitionsCount.forEach((k,v) -> addTransitionCount(k,v));
	}
	
	public void buildTransitions(List<State> seqStates) {
		int s = 0;
		while(s<count && s<seqStates.size()-1) {
			State curr = seqStates.get(s);
			State next = seqStates.get(s+1);
			String transition="";
			if(next.getStatus().equals(Status.MARKED) && TemporaryDataTransporter.shouldHighlight)
				transition = addColorTransitionWithArrowBetweenSameStates(curr,next, FSMConstants.LIME_GREEN_COLOR, FSMConstants.GREEN_COLOR);
			else 
				transition = addTransition(curr, next, next.getStatus());
			transitionsCount.merge(transition, 1, Integer::sum);
			s++;
		}
		printTransitions();
	}

}
