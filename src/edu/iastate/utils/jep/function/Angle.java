/*****************************************************************************
 JEP - Java Math Expression Parser 2.24
      December 30 2002
      (c) Copyright 2002, Nathan Funk
      See LICENSE.txt for license information.

 *****************************************************************************/

package edu.iastate.utils.jep.function;

import java.util.Stack;

import edu.iastate.utils.jep.ParseException;

public class Angle
    extends PostfixMathCommand
{
    public Angle()
    {
        numberOfParameters = 2;
    }

    public void run(Stack inStack) throws ParseException
    {
        checkStack(inStack); // check the stack
        Object param2 = inStack.pop();
        Object param1 = inStack.pop();

        if ( (param1 instanceof Number) && (param2 instanceof Number))
        {
            double x = ( (Number) param1).doubleValue();
            double y = ( (Number) param2).doubleValue();
            inStack.push(new Double(Math.atan2(x, y))); //push the result on the inStack
        }
        else
        {
            throw new ParseException("Invalid parameter type");
        }
        return;
    }
}
