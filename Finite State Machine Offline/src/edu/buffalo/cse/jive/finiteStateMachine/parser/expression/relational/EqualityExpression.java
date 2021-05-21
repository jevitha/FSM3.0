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
public class EqualityExpression extends RelationalExpression {

	public EqualityExpression(ValueExpression expressionA, ValueExpression expressionB) {
		super(expressionA, expressionB);
	}

	@Override
	public Boolean evaluate(Context context) {
		getExpressionA().evaluate(context);
		getExpressionB().evaluate(context);
		Pair<Boolean, Boolean> pair =  FSMUtil.validateStateAbstraction(getExpressionA().getValue(), getExpressionB().getValue(),"=");
		if(pair.getLeft())
			return pair.getRight();
		
		return getExpressionA().compareTo(getExpressionB()) == 0;
	}
}
