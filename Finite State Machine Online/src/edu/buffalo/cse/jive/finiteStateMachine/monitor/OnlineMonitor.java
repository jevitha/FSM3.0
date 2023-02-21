package edu.buffalo.cse.jive.finiteStateMachine.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import edu.buffalo.cse.jive.finiteStateMachine.FSMConstants;
import edu.buffalo.cse.jive.finiteStateMachine.models.Event;
import edu.buffalo.cse.jive.finiteStateMachine.models.State;
import edu.buffalo.cse.jive.finiteStateMachine.models.TransitionBuilder;
import edu.buffalo.cse.jive.finiteStateMachine.parser.Parser;
import edu.buffalo.cse.jive.finiteStateMachine.parser.TopDownParser;
import edu.buffalo.cse.jive.finiteStateMachine.parser.expression.expression.Expression;
import edu.buffalo.cse.jive.finiteStateMachine.parser.expression.value.StringValueExpression;
import edu.buffalo.cse.jive.finiteStateMachine.parser.expression.value.ValueExpression;
import edu.buffalo.cse.jive.finiteStateMachine.util.FSMUtil;
import edu.buffalo.cse.jive.finiteStateMachine.views.SvgGenerator;
/**
 * @author Jevitha KP
 * @email kp_jevitha@cb.amrita.edu
 *
 * @author Shashank Raghunath
 * @email sraghuna@buffalo.edu
 *
 */
public class OnlineMonitor extends Monitor {

	int portNumber;
	Job job ;
	List<Expression> expressions;
	String propertyExprStr;
	String propAbstrStr;
	private TransitionBuilder transitionBuilder;
	private SvgGenerator svgGenerator;
	private boolean countTransitions;
	private boolean doView=false, doVerify=false;

	public OnlineMonitor(Set<String> keyFields, BlockingQueue<Event> source, 
			String properties, String propAbstractions,
			boolean countTransitions, SvgGenerator svgGenerator,
			boolean view, boolean verify, boolean dataAbstraction) {

		super(keyFields, source, false, dataAbstraction);
		this.propertyExprStr = properties;
		this.propAbstrStr = propAbstractions;
		this.countTransitions = countTransitions;
		this.svgGenerator = svgGenerator;
		this.doView = view;
		this.doVerify = verify;
		
	}



	@Override
	public void run() {
		expressions = parseExpressions (propertyExprStr);
		job = new Job("EventProcessingJob") {

			protected IStatus run(IProgressMonitor monitor) {
				//monitor.beginTask("Monitoring Started", 10);
				boolean stateAdded=false;

				System.out.println("OnlineMonitor - run : Job Started : "+job.getName());
				//jevitha
				while(getSource().isEmpty()) {
					System.out.println("OnlineMonitor - run :  Waiting for events");

					if (monitor.isCanceled())  {
						System.out.println("OnlineMonitor - run :  Job Cancelled : "+ job.getName());
						return Status.CANCEL_STATUS;
					}

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				while (true) {
					if (monitor.isCanceled()) { 
						System.out.println("OnlineMonitor - run :   Job Cancelled : "+ job.getName());
						break;
					}
					try {
						Event event = getSource().take();
						System.out.println("OnlineMonitor - run : "+event.toString());
						
						stateAdded = buildStates(event);
						if(stateAdded) System.out.println("OnlineMonitor - run : New state : "+ stateAdded);
						int count = Integer.MAX_VALUE;
						//abstraction();
						if(stateAdded) {
							//abstraction 
							if (!propAbstrStr.equals("")) {
								if(stateAdded)	System.out.println("OnlineMonitor - run : Invoking Abstraction");
								abstraction();
								
							}

							
							if (expressions != null && expressions.size() > 0) {
								//monitor = new OfflineMonitor(keyAttributes, incomingEvents, granularity[1].getSelection());
								//monitor.run();
								
								
								//Online Verification
								try {
									if(doVerify == true) {
										if (validate(expressions)) {
											//									errorText.setText("All properties satisfied.                                 ");
											System.out.println("OnlineMonitor - run : Property satisfied");

										}
										else {
											System.err.println("OnlineMonitor - run : Property Violated");
//																				errorText.setText("Property Violated :                               ");
										}
									}

								} catch (Exception e1) {
									// TODO Auto-generated catch block
									System.err.println("OnlineMonitor - run :"+e1.getMessage());
									e1.printStackTrace();
								}


							}
							//transitionBuilder = new TransitionBuilder(monitor.getRootState(), monitor.getStates(), transitionCount.getSelection(), monitor.getSeqState(), count);
							transitionBuilder = new TransitionBuilder(getRootState(), getStates(), countTransitions, getSeqState(), count);
							transitionBuilder.build();
							if(doView == true)
								svgGenerator.generate(transitionBuilder.getTransitions());
						}
						//buildStates(getSource().take());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				return Status.CANCEL_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}




	public void stopMonitoring() {
		if(job != null)
			job.cancel();
		//		svgGenerator.generate("@startuml [*] --> [*] @enduml ");
	}

	protected void validateAbstractedState() {
		//		monitor.setStates(new HashMap<>());
		setStates(new HashMap<>());
		//		List<State> seqStates = monitor.getSeqState();
		List<State> seqStates = getSeqState();
		int s = 0;
		//		monitor.setStates(new HashMap<>());
		setStates(new HashMap<>());

		//		State previousState = monitor.getRootState();
		State previousState = getRootState();
		Map<State, Set<State>> states = new LinkedHashMap<>();

		while (s<seqStates.size()) {
			State newState = seqStates.get(s);
			if(s==0) {
				states.put(newState, new LinkedHashSet<State>());
				//				monitor.setRootState(newState);
				setRootState(newState);
			} else {
				states.get(previousState).add(newState);
				if (!states.containsKey(newState))
					states.put(newState, new LinkedHashSet<State>());
			}
			previousState = newState;
			s++;
		}

		//		monitor.setStates(states);
		setStates(states);
	}

	public void abstraction() {
		List<State> seqStates = getSeqState();
		//		String paStr = absText.getText().trim();
		if (propAbstrStr.equals(""))
			return;

		//If abstraction is given and in the property any future variable is present like a'
		//then we can't do abstraction on that
		Pattern p = Pattern.compile(FSMConstants.REGEX);
		//		Matcher m = p.matcher(propertyText.getText()); 
		Matcher m = p.matcher(propertyExprStr); 

		if(m.find()) {
			throw new IllegalArgumentException("The primed variable inside Property is not compatible with Abstraction");
		}

		p = Pattern.compile(FSMConstants.OR_REGEX);
		//		m = p.matcher(propertyText.getText()); 
		m = p.matcher(propertyExprStr); 
		if(m.find()) {
			throw new IllegalArgumentException("Property checking with abstraction : Replace OR expression with AND");
		}

		String[] paEntries = propAbstrStr.split(",");
		boolean reductionFlag = false;
		int s = 0;

		while (s<seqStates.size()) {
			List<ValueExpression> valueList = new ArrayList<>(seqStates.get(s).getVector().values());
			List<String> keyList = new ArrayList<>(seqStates.get(s).getVector().keySet());
			for (int k=0; k<paEntries.length && k<valueList.size(); k++) {
				if ( !paEntries[k].trim().equals("") ) {
					String absVal = FSMUtil.applyAbstraction(valueList.get(k).toString(), paEntries[k]);
					if (absVal.equals("")) {
						reductionFlag = true;
						break;
					}
					else {
						if(s!=0) {
							Map<String, ValueExpression> map = seqStates.get(s).getVector();
							map.put(keyList.get(k), new StringValueExpression(absVal));
						} else {
							State initial = seqStates.get(s).copy();
							Map<String, ValueExpression> map = initial.getVector();
							map.put(keyList.get(k), new StringValueExpression(absVal));
							seqStates.set(0, initial);
							//monitor.getRootState().getVector().put(keyList.get(k), new StringValueExpression(absVal));
						}
					}
				}
			}	
			if (reductionFlag) {
				State hState = seqStates.get(s).copy();
				//hState.hashed = true; //check this
				seqStates.set(s, hState);
				reductionFlag = false;
				s++;
			}
			else
				s++;
		}
		validateAbstractedState();
	}
	
	protected void pathAbstraction(Event event) {
		// TODO Auto-generated method stub
		
	}


	/**
	 * Reads property text and parses them into a list of expressions
	 * 
	 * @param propertyText
	 * @return List of expressions
	 * @throws Exception
	 */
	private List<Expression> parseExpressions(String propertyText){ //throws Exception {
		//if (propertyText != null && propertyText.getText().length() > 0) {
		List<Expression>  exp = null;
		if (propertyText.length() > 0) {
			Parser parser = new TopDownParser();
			String properties = propertyText.trim();
			try {
				exp = parser.parse(properties.split(";"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		return exp;
		//		throw new IllegalArgumentException("Please enter properties to validate");
	}
	
	
	public String getTransitionsForExport() {
		return transitionBuilder.getTransitions();
	}

}
