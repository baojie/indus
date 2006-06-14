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

public class ArcCosineH
    extends PostfixMathCommand
{
    public ArcCosineH()
    {
        numberOfParameters = 1;
    }

    public void run(Stack inStack) throws ParseException
    {
        checkStack(inStack); // check the stack
        Object param = inStack.pop();
        inStack.push(acosh(param)); //push the result on the inStack
        return;
    }

    public Object acosh(Object param) throws ParseException
    {
        if (param instanceof Number)
        {
            Complex temp = new Complex( ( (Number) param).doubleValue(), 0.0);

            return temp.acosh();
        }
        else if (param instanceof Complex)
        {
            return ( (Complex) param).acosh();
        }

        throw new ParseException("Invalid parameter type");
    }
}
