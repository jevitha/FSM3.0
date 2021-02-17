package edu.buffalo.cse.jive.finiteStateMachine.util;

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
	
}
