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
 * All function classes must implement this interface to ensure that the run()
 * method is implemented.
 */
public interface PostfixMathCommandI
{
    /**
     * Run the function on the stack. Pops the arguments from the stack, and
     * pushes the result on the top of the stack.
     */
    public void run(Stack aStack) throws ParseException;

    /**
     * Returns the number of required parameters, or -1 if any number of
     * parameters is allowed.
     */
    public int getNumberOfParameters();

    /**
     * Sets the number of current number of parameters used in the next call
     * of run(). This method is only called when the reqNumberOfParameters is
     * -1.
     */
    public void setCurNumberOfParameters(int n);
}
