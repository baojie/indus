/*****************************************************************************
 JEP - Java Math Expression Parser 2.24
      December 30 2002
      (c) Copyright 2002, Nathan Funk
      See LICENSE.txt for license information.

 *****************************************************************************/
/* Generated By:JJTree: Do not edit this line. ASTStart.java */
package edu.iastate.utils.jep;

/**
 * Start Node
 */
public class ASTStart
    extends SimpleNode
{
    public ASTStart(int id)
    {
        super(id);
    }

    public ASTStart(Parser p, int id)
    {
        super(p, id);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }
}
