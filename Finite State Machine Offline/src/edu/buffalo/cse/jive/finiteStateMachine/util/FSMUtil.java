package edu.buffalo.cse.jive.finiteStateMachine.util;

import edu.buffalo.cse.jive.finiteStateMachine.FSMConstants;

public class FSMUtil {

	public static String applyAbstraction(String value, String predicate) {

		if (value.equals("null"))
			return value;
		
		int choice;
		char lhs = predicate.trim().charAt(0);
		if ( !(lhs == '=' || lhs == '>' || lhs == '<' || lhs == '[' || lhs == '#' || lhs == '~') )
			return value;	// Invalid predicate - return 'value' as it is
		
		String rhs = predicate.trim().substring(1).trim();
		if (lhs == '#')
			choice = 4;
		else if (rhs.matches("^[0-9]+$")) 
			choice = 0; // integer
		else if (rhs.matches("^[0-9]+.[0-9]+$"))
			choice = 1; // decimal
		else if (rhs.endsWith("]"))
			choice = 2; // range
		else 
			choice = 3; // string

		switch(choice) {
		case 0:	// Integer
			int n = Integer.parseInt(predicate.substring(1).trim());
			if (predicate.trim().startsWith("<")) { // <n
				if ( Integer.parseInt(value) < n ) 
					return "<" + n;
				else
					return ">=" + n;
			}
			else if (predicate.trim().startsWith(">")) { // >n
				if ( Integer.parseInt(value) > n ) 
					return ">" + n;
				else
					return "<=" + n;
			}
			else if (predicate.trim().startsWith("=")) { // =n
				if ( Integer.parseInt(value) == n ) 
					return "" + n;
				else
					return "~" + n;
			}			
			else if (predicate.trim().startsWith("~")) { // ~n
				if ( Integer.parseInt(value) != n ) 
					return "~" + n;
				else
					return "" + n;
			}			
			break;
			
		case 1:	// Double
			double d = Double.parseDouble(predicate.substring(1).trim());
			if (predicate.trim().startsWith("<")) { // <n
				if ( Double.parseDouble(value) < d ) 
					return "<" + d;
				else
					return ">=" + d;
			}
			else if (predicate.trim().startsWith(">")) { // >n
				if ( Double.parseDouble(value) > d ) 
					return ">" + d;
				else
					return "<=" + d;
			}
			else if (predicate.trim().startsWith("=")) { // =n
				if ( Double.parseDouble(value) == d ) 
					return "" + d;
				else
					return "~" + d;
			}						
			else if (predicate.trim().startsWith("~")) { // ~n
				if ( Double.parseDouble(value) != d ) 
					return "~" + d;
				else
					return "" + d;
			}						
			break;
			
		case 2: // Range abstraction
			String[] range = predicate.substring(1, predicate.indexOf(']')).split(":");
			try {
				if (Integer.parseInt(value) < Integer.parseInt(range[0]))
					return "<" + Integer.parseInt(range[0]);
				for (int i=0; i < range.length-1; i++) {
					if ( Integer.parseInt(value) >= Integer.parseInt(range[i])
						&& Integer.parseInt(value) < Integer.parseInt(range[i+1])
					)
					return Integer.parseInt(range[i]) + ":" + Integer.parseInt(range[i+1]);
				}
				if (Integer.parseInt(value) >= Integer.parseInt(range[range.length-1]))
					return ">=" + Integer.parseInt(range[range.length-1]);
			}
			catch(NumberFormatException nfe) {
				return value;
			}
			break;
			
		case 3: // String
			String s = predicate.substring(1).trim();
			if (predicate.trim().startsWith("=")) {
				if (value.equals(s))
					return s;
				else
					return "~" + s;
			}
			else if (predicate.trim().startsWith("~")) {
				if (!value.equals(s))
					return "~" + s;
				else
					return s;
			}
			
		case 4: // Reduction or Selection
			String ss = predicate.substring(1).trim();
			if (predicate.trim().startsWith("#") && value.equals(ss))
				return ss;
			else
				return "";
		}
		return value; // If nothing matches return 'value' as is - no abstraction applied
	}
	
	public static Range<? extends Number> getRangeAbstraction(String absVal) {
		String[] arrRange = absVal.split(":");
		Range<Double> range = new Range<Double>();
		range.setStart(Double.parseDouble(arrRange[0]));
		range.setEnd(Double.parseDouble(arrRange[1])-(0.1*Math.pow(10, -10)));
		return range;
	}
	
	public static Range<? extends Number> getRange(Object value) {
		String absVal = value.toString();
		if(absVal.contains(":")) {
			//For Range abstraction values like [0:2]
			return getRangeAbstraction(absVal);
		}
		int length = absVal.length();
		boolean isEqualSign = false;
		boolean isGreaterSign = false;
		boolean isLessSign = false;
		boolean isNotEqualSign = false;
		
		StringBuilder builder = new StringBuilder();
		int i=0;
		while(i<length && !Character.isDigit(absVal.charAt(i))) {
			if(absVal.charAt(i)=='>')
				isGreaterSign = true;
			else if(absVal.charAt(i)=='<')
				isLessSign = true;
			else if(absVal.charAt(i)=='~')
				isNotEqualSign = true;
			else
				isEqualSign = true;
			builder.append(absVal.charAt(i));
			i++;
		}
	
		Range<Double> range = new Range<Double>();
		double nonAbsVal = Double.parseDouble(absVal.substring(i));
		range.setNonAbsValue(nonAbsVal);
		if(isGreaterSign) {
			range.setEnd(Double.MAX_VALUE);
			if(isEqualSign)
				range.setStart(nonAbsVal);
			else
				range.setStart(nonAbsVal+(0.1*Math.pow(10, -10)));//change to constants
		} 
		else if(isLessSign) {
			range.setStart(-Double.MAX_VALUE);
			if(isEqualSign)
				range.setEnd(nonAbsVal);
			else {
				range.setEnd(nonAbsVal-(0.1*Math.pow(10, -10)));
			}
		}
		else if(isNotEqualSign) {
			//~ sign
			range.setStart(-1*Double.MAX_VALUE);
			range.setEnd(Double.MAX_VALUE);
			range.setExcludeValue(nonAbsVal);
		}
		else {
			System.out.println("iseq "+nonAbsVal);
			range.setStart(nonAbsVal);
			range.setEnd(nonAbsVal);
		}
		return range;
	}
	
	public static Object shiftOperator(String op, Object value) {
		if(op.equals("<"))
			value = ">"+value;
		else if(op.equals(">"))
			value = "<"+value;
		else if(op.equals("<="))
			value = ">="+value;
		else if(op.equals(">="))
			value = "<="+value;
		else
			value = op+value; //= or ~
		return value;
	}
	
	public static Pair<String, Boolean> checkForOperator(String value) {
		Pair<String, Boolean> pair = null;
		if(value.contains("~"))
			pair = new Pair<>(value.substring(1), Boolean.TRUE);
		else
			pair = new Pair<>(value, Boolean.FALSE);
		return pair;
	}
	
	public static Pair<Boolean, Boolean> check(Object value1, Object value2, String op) {
		
		String val1Class = value1.getClass().getSimpleName();
		String val2Class = value2.getClass().getSimpleName();
		
		if(op.equals("!="))
			op = "~"; // to be in sync with state operator for not equal to
		Boolean isAbstraction = Boolean.TRUE;
		Boolean propertyCheck = Boolean.FALSE;
		if(val1Class.equals("Double") || val2Class.equals("Double")
				|| val1Class.equals("Integer") || val2Class.equals("Integer")) {
			/*If abstraction is not applied then directly we can compare*/
			if(!val1Class.equals("String") 
					&& !val2Class.equals("String"))
			{
				isAbstraction = Boolean.FALSE;
				return new Pair<Boolean, Boolean>(isAbstraction, propertyCheck);
			}
			Object rangeObject = null;
			Object definitveObject = null;
			boolean isFirst = false;
			if(val1Class.equals("String")) {
				rangeObject = value1;
				definitveObject = value2;
			} else {
				rangeObject = value2;
				definitveObject = value1;
				isFirst = true;
			}
			//String classType = definitveObject.getClass().getSimpleName();
			
			if(isFirst)
				definitveObject = shiftOperator(op, definitveObject);
			else 
				definitveObject = op+definitveObject;
			Range<?> range1 = getRange(rangeObject);
			Range<?> range2 = getRange(definitveObject);
			
			double startA = range1.getStart().doubleValue();
			double endA = range1.getEnd().doubleValue();
			double startB = range2.getStart().doubleValue();
			double endB = range2.getEnd().doubleValue();
			System.out.println("range1 start "+startA);
			System.out.println("range1 end "+endA);
			System.out.println("range2 start "+startB);
			System.out.println("range2 end "+endB);
			System.out.println((startA>=startB && startA<=endB));
			System.out.println((endA>=startB && endA<=endB));
			/*
			 * the first if and else if, solves all the case for equal to and negation
			 * r!=0 -> r=0 (else if case)
			 * r!=0 -> r!=0 (if case)
			 * r=0 -> r!=0 (if case)
			 * r=0 -> r=0 (if case)
			 */
			if((startA>=startB && startA<=endB) && (endA>=startB && endA<=endB)) {
				propertyCheck = true;
				if(range1.getExcludeValue()==null && range2.getExcludeValue() != null) {
					//state value is =0 and property is !=0
					double excludeVal = range2.getExcludeValue().doubleValue();
					if(startA==excludeVal || endA==excludeVal) {
						System.out.println("yum");
						propertyCheck = false;
					}
				}
				else if(range1.getExcludeValue() != null && range2.getExcludeValue() != null) {
					//~ in state value and != in given property value
					double excludeVal1 = range1.getExcludeValue().doubleValue();
					double excludeVal2 = range2.getExcludeValue().doubleValue();
					if(excludeVal1 != excludeVal2) {
						System.out.println("prop");
						propertyCheck = false;
					}
				}
			}
			else if (!(startA>=startB && startA<=endB) && !(endA>=startB && endA<=endB)) {
				propertyCheck = false;
			}
			else
				throw new IllegalArgumentException(FSMConstants.INVALID_ABSTRACTION_MSG);
			
			return new Pair<Boolean, Boolean>(isAbstraction, propertyCheck);
		}
		else if(val1Class.equals("String") && val2Class.equals("String")) {
			System.out.println("yup");
			if(!value1.toString().contains("~") 
					&& !value2.toString().contains("~"))
			{
				isAbstraction = Boolean.FALSE;
				return new Pair<Boolean, Boolean>(isAbstraction, propertyCheck);
			}
			System.out.println("venu");
			/*If abstraction is applied on string it will be 
			=(this is usually ignored in state value) or !=(i.e ~)*/
			String val1Str = value1.toString();
			String val2Str = value2.toString();
			Pair<String, Boolean> pair1 = checkForOperator(val1Str);
			val1Str = pair1.getLeft();
			boolean val1Op = pair1.getRight();
			Pair<String, Boolean> pair2 = checkForOperator(val2Str);
			val2Str = pair2.getLeft();
			boolean val2Op = pair2.getRight();
			
			if(val1Str.equals(val2Str)) {
				if(!val1Op && !val2Op) {
					if(op.equals("="))
						propertyCheck = true;
					else
						propertyCheck = false;
				} else {
					if(op.equals("="))
						propertyCheck = false;
					else
					 	propertyCheck = true;
				}
			} else {
				if(!val1Op && !val2Op) {
					if(op.equals("="))
						propertyCheck = false;
					else
						propertyCheck = true;
				} else
					throw new IllegalArgumentException(FSMConstants.INVALID_ABSTRACTION_MSG);
			}
			
			return new Pair<Boolean, Boolean>(isAbstraction, propertyCheck);
		}
		throw new IllegalArgumentException("Type mismatch in properties");
	}
	
}
