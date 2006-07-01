package edu.iastate.utils.sql;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import edu.iastate.utils.Debug;
import edu.iastate.utils.lang.MessageHandler;
import edu.iastate.utils.lang.MessageMap;
import edu.iastate.utils.string.ParserUtils;

/**
 * A simple SQL editor
 * @author Jie Bao
 * @since 1.0 2005-03-18
 */
public class SQLPanel extends JPanel implements MessageHandler
{
    public JButton   btnCopy      = new JButton("Copy(Selected)");
    public JButton   btnPaste     = new JButton("Paste");

    public JPanel    buttonPanel  = new JPanel();
    JLabel           labelInfo      = new JLabel();
    JScrollPane      jScrollPane1 = new JScrollPane();
    public JTextArea sqlInput     = new JTextArea();

    public SQLPanel()
    {
        try
        {
            jbInit();
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }
    
    // Jie Bao 2006-06-30
    public void setInfo(String s)
    {
        labelInfo.setText(s);
    }

    private void jbInit() throws Exception
    {
        messageMap();

        labelInfo.setText("Input SQL sentence here");
        //(example SELECT * FROM mytable WHERE ID=1);");
        sqlInput.setBorder(BorderFactory.createEtchedBorder());
        sqlInput.setWrapStyleWord(true);
        this.setLayout(new BorderLayout());

        buttonPanel.add(btnCopy, null);
        buttonPanel.add(btnPaste, null);

        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        jScrollPane1.getViewport().add(sqlInput, null);

        this.add(jScrollPane1, java.awt.BorderLayout.CENTER);
        this.add(buttonPanel, java.awt.BorderLayout.SOUTH);
        this.add(labelInfo, java.awt.BorderLayout.NORTH);
    }

    /**
     * @author Jie Bao
     * @since 2005-03-18
     */
    public void messageMap()
    {
        try
        {
            MessageMap.mapAction(btnCopy, this, "onCopy");
            MessageMap.mapAction(btnPaste, this, "onPaste");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }

    public void onCopy(ActionEvent e)
    {
        Debug.trace("SQLPanel.onCopy() -" + sqlInput.getText());
        sqlInput.copy();
    }

    public void onPaste(ActionEvent e)
    {
        sqlInput.paste();
    }

    /**
     * Check the validity of the SQL sentence
     *
     * @param strSQL
     * @return
     * @author Jie Bao
     * @version 2003-11-11
     */
    public boolean verifySQL(String strSQL)
    {
        /* model
         SELECT XXX
         FROM YYY
         [ WHERE ZZZ ];
         */
        String pattern = ParserUtils.CASE_INSENSITIVE + // case insensitive
                ParserUtils.DOTALL + // CR and LF are ignored
                "SELECT" + ParserUtils.BLANKS + ParserUtils.ANY_WORD
                + ParserUtils.BLANKS + "FROM" + ParserUtils.BLANKS
                + ParserUtils.ANY_WORD
        //+ ";"
        ;
        return ParserUtils.isFound(pattern, strSQL);
    }

    public String getSqlInput()
    {
        return sqlInput.getText();
    }

    public void setSqlInput(String str)
    {
        sqlInput.setText(str);
    }

}
