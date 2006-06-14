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

public class NaturalLogarithm
    extends PostfixMathCommand
{
    public NaturalLogarithm()
    {
        numberOfParameters = 1;

    }

    public void run(Stack inStack) throws ParseException
    {
        checkStack(inStack); // check the stack
        Object param = inStack.pop();
        inStack.push(ln(param)); //push the result on the inStack
        return;
    }

    public Object ln(Object param) throws ParseException
    {
        if (param instanceof Number)
        {
            // TODO: think about only returning Complex if param is <0
            Complex temp = new Complex( ( (Number) param).doubleValue());
            return temp.log();
        }
        else if (param instanceof Complex)
        {
            return ( (Complex) param).log();
        }

        throw new ParseException("Invalid parameter type");
    }
}
