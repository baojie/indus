package edu.iastate.anthill;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public final class test
    extends JFrame
{
    public test()
    {
        try
        {
            jbInit();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        //MIPSIterator.test();
        //remove "\n"



    }

    private void jbInit() throws Exception
    {
        jLabel1.setText("jLabel1");
        jLabel1.addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                jLabel1_mouseClicked(e);
            }
        });
        this.getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);
        jPanel1.add(jLabel1);
        this.setSize(800, 600);
    }

    JPanel jPanel1 = new JPanel();
    JLabel jLabel1 = new JLabel();
    public void jLabel1_mouseClicked(MouseEvent e)
    {

    }

}
