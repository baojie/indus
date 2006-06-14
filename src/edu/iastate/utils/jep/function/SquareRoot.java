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

public class SquareRoot
    extends PostfixMathCommand
{
    public SquareRoot()
    {
        numberOfParameters = 1;
    }

    /**
     * Applies the function to the parameters on the stack.
     */
    public void run(Stack inStack) throws ParseException
    {

        checkStack(inStack); // check the stack
        Object param = inStack.pop();
        inStack.push(sqrt(param)); //push the result on the inStack
        return;
    }

    /**
     * Calculates the square root of the parameter. The parameter must
     * either be of type Double or Complex.
     *
     * @return The square root of the parameter.
     */
    public Object sqrt(Object param) throws ParseException
    {

        if (param instanceof Number)
        {
            double value = ( (Number) param).doubleValue();

            // a value less than 0 will produce a complex result
            if (value < 0)
            {
                return (new Complex(value).sqrt());
            }
            else
            {
                return new Double(Math.sqrt(value));
            }
        }
        else if (param instanceof Complex)
        {
            return ( (Complex) param).sqrt();
        }

        throw new ParseException("Invalid parameter type");
    }
}
