/*****************************************************************************
 JEP - Java Math Expression Parser 2.24
      December 30 2002
      (c) Copyright 2002, Nathan Funk
      See LICENSE.txt for license information.

 *****************************************************************************/

package edu.iastate.utils.jep.type;

/**
 * Default class for creating number objects. This class can be replaced by
 * other NumberFactory implementations if other number types are required. This
 * can be done using the
 */
public class DoubleNumberFactory
    implements NumberFactory
{

    /**
     * Creates a Double object initialized to the value of the parameter.
     *
     * @param value The initialization value for the returned object.
     */
    public Object createNumber(double value)
    {
        return new Double(value);
    }
}
