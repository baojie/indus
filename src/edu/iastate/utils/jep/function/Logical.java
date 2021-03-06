/*****************************************************************************
 JEP - Java Math Expression Parser 2.24
      December 30 2002
      (c) Copyright 2002, Nathan Funk
      See LICENSE.txt for license information.

 *****************************************************************************/

package edu.iastate.utils.jep.function;

import java.util.Stack;

import edu.iastate.utils.jep.ParseException;

public class Logical
    extends PostfixMathCommand
{
    int id;

    public Logical(int id_in)
    {
        id = id_in;
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
            int r;

            switch (id)
            {
                case 0:

                    // AND
                    r = ( (x != 0d) && (y != 0d)) ? 1 : 0;
                    break;
                case 1:

                    // OR
                    r = ( (x != 0d) || (y != 0d)) ? 1 : 0;
                    break;
                default:
                    r = 0;
            }

            inStack.push(new Double(r)); // push the result on the inStack
        }
        else
        {
            throw new ParseException("Invalid parameter type");
        }
        return;
    }
}
