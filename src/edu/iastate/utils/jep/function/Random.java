/*****************************************************************************
 JEP - Java Math Expression Parser 2.24
      December 30 2002
      (c) Copyright 2002, Nathan Funk
      See LICENSE.txt for license information.

 *****************************************************************************/
package edu.iastate.utils.jep.function;

import java.util.Stack;

import edu.iastate.utils.jep.ParseException;

/**
 * Encapsulates the Math.random() function.
 */
public class Random
    extends PostfixMathCommand
{
    public Random()
    {
        numberOfParameters = 0;

    }

    public void run(Stack inStack) throws ParseException
    {
        checkStack(inStack); // check the stack
        inStack.push(new Double(Math.random()));
        return;
    }
}
