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

public class SineH
    extends PostfixMathCommand
{
    public SineH()
    {
        numberOfParameters = 1;
    }

    public void run(Stack inStack) throws ParseException
    {
        checkStack(inStack); // check the stack
        Object param = inStack.pop();
        inStack.push(sinh(param)); //push the result on the inStack
        return;
    }

    public Object sinh(Object param) throws ParseException
    {
        if (param instanceof Number)
        {
            double value = ( (Number) param).doubleValue();
            return new Double( (Math.exp(value) - Math.exp( -value)) / 2);
        }
        else if (param instanceof Complex)
        {
            return ( (Complex) param).sinh();
        }

        throw new ParseException("Invalid parameter type");
    }

}
