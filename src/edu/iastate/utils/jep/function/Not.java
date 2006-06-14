/*****************************************************************************
 JEP - Java Math Expression Parser 2.24
      December 30 2002
      (c) Copyright 2002, Nathan Funk
      See LICENSE.txt for license information.

 *****************************************************************************/
package edu.iastate.utils.jep.function;

import java.util.Stack;

import edu.iastate.utils.jep.ParseException;

public class Not
    extends PostfixMathCommand
{
    public Not()
    {
        numberOfParameters = 1;

    }

    public void run(Stack inStack) throws ParseException
    {
        checkStack(inStack); // check the stack
        Object param = inStack.pop();
        if (param instanceof Number)
        {
            int r = ( ( (Number) param).doubleValue() == 0) ? 1 : 0;
            inStack.push(new Double(r)); //push the result on the inStack
        }
        else
        {
            throw new ParseException("Invalid parameter type");
        }
        return;
    }

}
