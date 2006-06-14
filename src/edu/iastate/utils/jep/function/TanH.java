/*****************************************************************************
 JEP - Java Math Expression Parser 2.24
      December 30 2002
      (c) Copyright 2002, Nathan Funk
      See LICENSE.txt for license information.

 *****************************************************************************/
package edu.iastate.utils.jep.function;

import java.util.Stack;

import edu.iastate.utils.jep.ParseException;
import edu.iastate.utils.jep.type.Complex;

public class TanH
    extends PostfixMathCommand
{
    public TanH()
    {
        numberOfParameters = 1;
    }

    public void run(Stack inStack) throws ParseException
    {
        checkStack(inStack); // check the stack
        Object param = inStack.pop();
        inStack.push(tanh(param)); //push the result on the inStack
        return;
    }

    public Object tanh(Object param) throws ParseException
    {
        if (param instanceof Number)
        {
            double value = ( (Number) param).doubleValue();
            return new Double( (Math.exp(value) - Math.exp( -value)) /
                              (Math.pow(Math.E, value) +
                               Math.pow(Math.E, -value)));
        }
        else if (param instanceof Complex)
        {
            return ( (Complex) param).tanh();
        }

        throw new ParseException("Invalid parameter type");
    }

}
