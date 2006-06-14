package edu.iastate.anthill.indus.query;

import Zql.ZConstant;

/**
 * @author Jie Bao
 * @since 1.0 2005-03-21
 */
public class ZConstantEx
    extends ZConstant
{
    public static int AVH = 4;

    public ZConstantEx(java.lang.String v, int typ)
    {
        super(v, typ);
    }

    public String toString()
    {
        if (getType() == AVH || getType() == STRING)
        {
            return "'" + getValue() + "'";
        }
        else
        {
            return getValue();
        }
    }
}
