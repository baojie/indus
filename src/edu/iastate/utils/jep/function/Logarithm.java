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

public class Logarithm
    extends PostfixMathCommand
{
    public Logarithm()
    {
        numberOfParameters = 1;
    }

    public void run(Stack inStack) throws ParseException
    {
        checkStack(inStack); // check the stack
        Object param = inStack.pop();
        inStack.push(log(param)); //push the result on the inStack
        return;
    }

    public Object log(Object param) throws ParseException
    {

        if (param instanceof Number)
        {

            Complex temp = new Complex( ( (Number) param).doubleValue());
            Complex temp2 = new Complex(Math.log(10), 0);
            return temp.log().div(temp2);

        }
        else if (param instanceof Complex)
        {

            Complex temp = new Complex(Math.log(10), 0);
            return ( (Complex) param).log().div(temp);
        }

        throw new ParseException("Invalid parameter type");
    }

}
