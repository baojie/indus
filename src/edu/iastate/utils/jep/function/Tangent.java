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

public class Tangent
    extends PostfixMathCommand
{
    public Tangent()
    {
        numberOfParameters = 1;
    }

    public void run(Stack inStack) throws ParseException
    {
        checkStack(inStack); // check the stack
        Object param = inStack.pop();
        inStack.push(tan(param)); //push the result on the inStack
        return;
    }

    public Object tan(Object param) throws ParseException
    {
        if (param instanceof Number)
        {
            return new Double(Math.tan( ( (Number) param).doubleValue()));
        }
        else if (param instanceof Complex)
        {
            return ( (Complex) param).tan();
        }

        throw new ParseException("Invalid parameter type");
    }

}
