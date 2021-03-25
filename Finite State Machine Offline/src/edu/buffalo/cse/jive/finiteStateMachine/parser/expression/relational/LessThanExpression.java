package edu.buffalo.cse.jive.finiteStateMachine.parser.expression.relational;

import edu.buffalo.cse.jive.finiteStateMachine.models.Context;
import edu.buffalo.cse.jive.finiteStateMachine.parser.expression.value.ValueExpression;
import edu.buffalo.cse.jive.finiteStateMachine.util.FSMUtil;
import edu.buffalo.cse.jive.finiteStateMachine.util.Pair;

/**
 * @author Shashank Raghunath
 * @email sraghuna@buffalo.edu
 *
 */
public class LessThanExpression extends RelationalExpression {
	
	public LessThanExpression(ValueExpression expressionA, ValueExpression expressionB) {
		super(expressionA, expressionB);
	}

	@Override
	public Boolean evaluate(Context context) {
		getExpressionA().evaluate(context);
		getExpressionB().evaluate(context);
		
		System.out.println("lt "+getExpressionA().getValue().toString());
		System.out.println("lt "+getExpressionB().getValue().toString());
		
		Pair<Boolean, Boolean> pair =  FSMUtil.check(getExpressionA().getValue(), getExpressionB().getValue(), "<");
		if(pair.getLeft())
			return pair.getRight();
		return getExpressionA().compareTo(getExpressionB()) < 0;
	}
}
