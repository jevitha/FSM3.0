package edu.buffalo.cse.jive.finiteStateMachine.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import edu.buffalo.cse.jive.finiteStateMachine.models.Context;
import edu.buffalo.cse.jive.finiteStateMachine.models.Event;
import edu.buffalo.cse.jive.finiteStateMachine.models.State;
import edu.buffalo.cse.jive.finiteStateMachine.models.State.Status;
import edu.buffalo.cse.jive.finiteStateMachine.parser.expression.expression.Expression;
import edu.buffalo.cse.jive.finiteStateMachine.parser.expression.temporal.EExpression;
import edu.buffalo.cse.jive.finiteStateMachine.util.TemporaryDataTransporter;

/**
 * @author Shashank Raghunath
 * @email sraghuna@buffalo.edu
 * 
 *@author Amlan Gupta
 *@email amlangup@buffalo.edu
 *
 */
public abstract class Monitor implements Runnable {

	private Set<String> keyFields;
	private BlockingQueue<Event> source;
	private Map<State, Set<State>> states; //Add transition count(set<State,Integer>)
	List<State> seqStates = new ArrayList<State>(); // Sequence of original states
	private State rootState;
	private State previousState;
	private boolean shouldConsolidateByMethod;
	private Map<String, State> consolidatedStateMap;
	private LinkedHashMap<String,Integer> transitionsCount;

	/**
	 * Initializes Key Fields, source, adjacency list and a dummy state
	 * 
	 * @param keyFields
	 * @param source
	 * @param shouldConsolidateByMethod 
	 */
	public Monitor(Set<String> keyFields, BlockingQueue<Event> source, boolean shouldConsolidateByMethod) {
		this.keyFields = keyFields;
		this.source = source;
		this.shouldConsolidateByMethod = shouldConsolidateByMethod;
		this.states = new HashMap<State, Set<State>>();
		this.transitionsCount = new LinkedHashMap<String,Integer>();
		this.consolidatedStateMap = new LinkedHashMap<String, State>();
		previousState = new State();
		for (String field : getKeyFields()) {
			if (Event.abbreviations.containsKey(field))
				previousState.getVector().put(Event.abbreviations.get(field), null);
			else
				previousState.getVector().put(field, null);
		}
	}

	/**
	 * Given an event, builds a state and adds it into the adjacency list
	 * 
	 * @param event
	 * @return
	 */
	protected boolean buildStates(Event event) {
		boolean result = false;
		if (keyFields.contains(event.getField()) || keyFields.contains(getEventKey(event.getField()))) {
			State newState = previousState.copy();
			newState.getVector().put(event.getField(), event.getValue());
			if (!newState.getVector().values().contains(null) && !previousState.getVector().values().contains(null)) {
				seqStates.add(newState);//newly added
				result = states.get(previousState).add(newState);
				if (!states.containsKey(newState))
					states.put(newState, new LinkedHashSet<State>());
			} else if (!newState.getVector().values().contains(null)
					&& previousState.getVector().values().contains(null)) {
				seqStates.add(newState);//newly added
				states.put(newState, new LinkedHashSet<State>());
				rootState = newState;
				System.out.println("rootstate "+rootState.getVector().values());
			}
			previousState = newState;
		}
		return result;
	}
	
	protected void generateConsolidateEventMap(Event event) {
		
		if (keyFields.contains(event.getField()) || keyFields.contains(getEventKey(event.getField()))) {
			State newState = previousState.copy();
			newState.getVector().put(event.getField(), event.getValue());
			consolidatedStateMap.put(event.getMethod(), newState);	
			previousState = newState;
		}
		
	}
	
	protected void buildConsolidatedStates() {
		
		this.previousState = new State();
		for (String field : getKeyFields()) {
			if (Event.abbreviations.containsKey(field))
				this.previousState.getVector().put(Event.abbreviations.get(field), null);
			else
				this.previousState.getVector().put(field, null);
		}
		
		int stateCount = 0;
		for(String key:consolidatedStateMap.keySet()) {
			
			State newState = consolidatedStateMap.get(key);
			
			if(stateCount==0) {
				seqStates.add(newState);
				states.put(newState, new LinkedHashSet<State>());
				rootState = newState;
			} else {
				/*String transition = MessageFormat.format(FSMConstants.TRANSITION, previousState.toString(), newState.toString());
				this.transitionsCount.merge(transition, 1, Integer::sum);*/
				seqStates.add(newState);
				states.get(previousState).add(newState);
				if (!states.containsKey(newState))
					states.put(newState, new LinkedHashSet<State>());
				
			}
			previousState = newState;
			stateCount++;
			
		}		
		
	}

	/**
	 * Simple helper function
	 * 
	 * @param value
	 * @return
	 */
	private String getEventKey(String value) {
		if (Event.abbreviations == null)
			return null;
		for (Map.Entry<String, String> entry : Event.abbreviations.entrySet()) {
			//System.out.println(entry.getKey()+" "+entry.getValue());
			if (entry.getValue().equals(value))
				return entry.getKey();
		}
		return null;
	}

	/**
	 * Validates the adjacency list against the list of properties
	 * 
	 * @param expressions
	 * @return
	 * @throws Exception
	 */
	public boolean validate(List<Expression> expressions) throws Exception {
		boolean result = validate(rootState, expressions);
		if(result && expressions.get(0) instanceof EExpression && TemporaryDataTransporter.shouldHighlight)rootState.setStatus(Status.MARKED);
		else if(result)rootState.setStatus(Status.VALID);
		else rootState.setStatus(Status.INVALID);
		return result;
	}

	public void resetStates() {
		for (State key : states.keySet()) {
			for (State state : states.get(key))
				state.reset();
		}
	}

	/**
	 * Hold tight, a long journey begins here.
	 * 
	 * @param root
	 * @param expressions
	 * @return
	 */
	private boolean validate(State root, List<Expression> expressions) {
		boolean valid = true;
		
		State nextState = null;  // BJ: there must be a next state
		System.out.println("check size "+states.size()+" "+states.keySet().toString()+" "+root.getVector().keySet());
		System.out.println(states.values().toString());
		System.out.println("valueexp "+root.getStatus().name()+" "+root.getVector().keySet()+" "+root.getVector().values());
		
		/*if(seqStates.size()>1) {
			System.out.println("hello "+seqStates.get(0));
			nextState = seqStates.get(1);
		}*/
		for (State n : states.get(root)) {
			nextState = n;
			break;
		}
		System.out.println("nextState "+nextState.getVector().keySet()+" "+nextState.getVector().values());
		Context thisContext = new Context(root, nextState, states); // nextState was null here and below
		if(expressions.get(0) instanceof EExpression)thisContext = new Context(root, nextState, states,true);
		
		for (Expression expression : expressions) {
			valid = expression.evaluate(thisContext) && valid;
		}
		
		return valid;
	}

	/**
	 * You're welcome
	 */
	protected void printStates() {
		for (State key : states.keySet()) {
			System.out.print(key + " : ");
			for (State state : states.get(key)) {
				System.out.print(state + " ");
			}
			System.out.println();
		}
	}

	public Set<String> getKeyFields() {
		return keyFields;
	}

	public void setKeyFields(Set<String> keyFields) {
		this.keyFields = keyFields;
	}

	public BlockingQueue<Event> getSource() {
		return source;
	}

	public void setSource(BlockingQueue<Event> source) {
		this.source = source;
	}

	public Map<State, Set<State>> getStates() {
		return states;
	}

	public void setStates(Map<State, Set<State>> states) {
		this.states = states;
	}

	public State getRootState() {
		return rootState;
	}
	
	public List<State> getSeqState() {
		return seqStates;
	}

	public void setSeqState(List<State> seqStates) {
		this.seqStates = seqStates;
	}

	public void setRootState(State rootState) {
		this.rootState = rootState;
	}

	public LinkedHashMap<String,Integer> getTransitionsCount() {
		return transitionsCount;
	}
	
	public boolean isShouldConsolidateByMethod() {
		return shouldConsolidateByMethod;
	}

	public void setShouldConsolidateByMethod(boolean shouldConsolidateByMethod) {
		this.shouldConsolidateByMethod = shouldConsolidateByMethod;
	}
	
	
}
