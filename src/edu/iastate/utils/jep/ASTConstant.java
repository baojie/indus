/*****************************************************************************
 JEP - Java Math Expression Parser 2.24
      December 30 2002
      (c) Copyright 2002, Nathan Funk
      See LICENSE.txt for license information.

 *****************************************************************************/
/* Generated By:JJTree: Do not edit this line. ASTInteger.java */
package edu.iastate.utils.jep;

/**
 * Constant Node
 */
public class ASTConstant
    extends SimpleNode
{
    private Object value;

    public ASTConstant(int id)
    {
        super(id);
    }

    public ASTConstant(Parser p, int id)
    {
        super(p, id);
    }

    public void setValue(Object val)
    {
        value = val;
    }

    public Object getValue()
    {
        return value;
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    public String toString()
    {
        return "Constant: " + value;
    }
}
