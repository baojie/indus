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

public class CosineH
    extends PostfixMathCommand
{
    public CosineH()
    {
        numberOfParameters = 1;
    }

    public void run(Stack inStack) throws ParseException
    {
        checkStack(inStack); // check the stack
        Object param = inStack.pop();
        inStack.push(cosh(param)); //push the result on the inStack
        return;
    }

    public Object cosh(Object param) throws ParseException
    {
        if (param instanceof Number)
        {
            double value = ( (Number) param).doubleValue();
            return new Double( (Math.exp(value) + Math.exp( -value)) / 2);
        }
        else if (param instanceof Complex)
        {
            return ( (Complex) param).cosh();
        }

        throw new ParseException("Invalid parameter type");
    }

}
