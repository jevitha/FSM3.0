package edu.buffalo.cse.jive.finiteStateMachine.util;

public class Range<T extends Number> {

	T start;
	
	T end;
	
	T nonAbsValue;
	
	T excludeValue=null;//for ~ in state value or != in property

	Range(){
		
	}

	public T getStart() {
		return start;
	}

	public void setStart(T start) {
		this.start = start;
	}

	public T getEnd() {
		return end;
	}

	public void setEnd(T end) {
		this.end = end;
	}

	public T getNonAbsValue() {
		return nonAbsValue;
	}

	public void setNonAbsValue(T nonAbsValue) {
		this.nonAbsValue = nonAbsValue;
	}

	public T getExcludeValue() {
		return excludeValue;
	}

	public void setExcludeValue(T excludeValue) {
		this.excludeValue = excludeValue;
	}
	
}
