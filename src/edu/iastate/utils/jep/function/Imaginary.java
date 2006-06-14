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

public class Imaginary
    extends PostfixMathCommand
{
    public Imaginary()
    {
        numberOfParameters = 1;
    }

    public void run(Stack inStack) throws ParseException
    {

        checkStack(inStack); // check the stack
        Object param = inStack.pop();
        inStack.push(im(param)); //push the result on the inStack
        return;
    }

    public Number im(Object param) throws ParseException
    {

        if (param instanceof Number)
        {
            return new Double(0);
        }
        else if (param instanceof Complex)
        {
            return new Double( ( (Complex) param).im());
        }

        throw new ParseException("Invalid parameter type");
    }

}
