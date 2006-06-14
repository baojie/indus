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

public class Comparative
    extends PostfixMathCommand
{
    int id;
    double tolerance;

    public Comparative(int id_in)
    {
        id = id_in;
        numberOfParameters = 2;
        tolerance = 1e-6;
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
                    r = (x < y) ? 1 : 0;
                    break;
                case 1:
                    r = (x > y) ? 1 : 0;
                    break;
                case 2:
                    r = (x <= y) ? 1 : 0;
                    break;
                case 3:
                    r = (x >= y) ? 1 : 0;
                    break;
                case 4:
                    r = (x != y) ? 1 : 0;
                    break;
                case 5:
                    r = (x == y) ? 1 : 0;
                    break;
                default:
                    throw new ParseException("Unknown relational operator");
            }

            inStack.push(new Double(r)); //push the result on the inStack
        }
        else if ( (param1 instanceof Complex) && (param2 instanceof Complex))
        {
            int r;

            switch (id)
            {
                case 4:
                    r = ( (Complex) param1).equals( (Complex) param2, tolerance) ?
                        0 : 1;
                    break;
                case 5:
                    r = ( (Complex) param1).equals( (Complex) param2, tolerance) ?
                        1 : 0;
                    break;
                default:
                    throw new ParseException("Relational operator type error");
            }

            inStack.push(new Double(r)); //push the result on the inStack
        }
        else if ( (param1 instanceof String) && (param2 instanceof String))
        {
            int r;

            switch (id)
            {
                case 4:
                    r = ( (String) param1).equals( (String) param2) ? 0 : 1;
                    break;
                case 5:
                    r = ( (String) param1).equals( (String) param2) ? 1 : 0;
                    break;
                default:
                    throw new ParseException("Relational operator type error");
            }

            inStack.push(new Double(r)); //push the result on the inStack
        }
        else
        {
            throw new ParseException("Invalid parameter type");
        }

        return;
    }
}
