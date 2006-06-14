package edu.iastate.anthill.indus.reasoner;

import java.util.Set;
import java.util.Vector;

import java.awt.event.ActionEvent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.TreeNode;

import edu.iastate.utils.lang.MessageHandler;
import edu.iastate.utils.lang.MessageMap;
import javax.swing.JFrame;
import javax.swing.*;

/**
 * The User interface for reasoner
 * @author Jie Bao
 * @since 2005-04-11
 */
public class ReasonerPanel
    extends ReasonerPanelGUI implements MessageHandler
{

    public ReasonerPanel(ShortBridgeReasoner reasoner)
    {
        super(reasoner);
        try
        {

            this.jbInit();

        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * @since 2005-04-11
     */
    public void messageMap()
    {
        //button handler
        try
        {
            MessageMap.mapAction(this.btnValidate, this, "onValidate");
            MessageMap.mapAction(this.btnSubsumption, this, "onSubsumption");
            MessageMap.mapAction(this.btnViewRule, this, "onViewRule");
            lstTerm.addListSelectionListener(new MyListSelectionListener());
        }
        catch (Exception ex)
        {
        }
    }

    /**
     * @since 2004-04-12
     * @param e ActionEvent
     */
    public void onViewRule(ActionEvent e)
    {
        ruleDlg.show();
    }

    /**
     * @since 2004-04-12
     * @param e ActionEvent
     */
    public void onSubsumption(ActionEvent e)
    {
        // ask for two terms
        Vector vvv = new Vector();
        for (int i = 0; i < lstTerm.getModel().getSize(); i++)
        {
            vvv.add(lstTerm.getModel().getElementAt(i));
        }

        Object[] data = vvv.toArray();
        if (data.length < 2)
        {
            String  info = "You should have at least two term to check subsumption";
            JOptionPane.showMessageDialog(this, info);
            return;
        }

        String term1 = (String) JOptionPane.showInputDialog(null,
            "Choose term 1", "Input", JOptionPane.INFORMATION_MESSAGE, null,
            data, data[0]);
        if (term1 != null)
        {
            vvv.remove(term1);

            String term2 = (String) JOptionPane.showInputDialog(null,
                "Choose term 2", "Input",
                JOptionPane.INFORMATION_MESSAGE, null,
                vvv.toArray(), vvv.elementAt(0));
            if (term2 != null)
            {
                // ask reasonr for subsumption
                boolean into = reasoner.isInto(term1, term2);
                String info = term1 + " INTO " + term2 + " : " + into;
                JOptionPane.showMessageDialog(this, info);
            }
        }

    }

    /**
     * @since 2004-04-11
     * @param e ActionEvent
     */
    public void onValidate(ActionEvent e)
    {
        boolean good = reasoner.isConsistent();
        String ok = "The mapping is consistent";
        String bad = "The mapping is inconsistent";

        String info = (good ? ok : bad + "\nDetails: " +
                       reasoner.badRuleInformation);
        JOptionPane.showMessageDialog(this, info);

    }

    private void jbInit() throws Exception
    {
        messageMap();

        Set termSet = reasoner.term2EqualClass.keySet();
        lstTerm.setListData(termSet.toArray());

        ecTree = reasoner.ECG.visualize();
        paneEC.getViewport().add(ecTree);

        JList list = new JList(reasoner.ruleSet.toArray());
        JScrollPane s = new JScrollPane(list);
        ruleDlg.getContentPane().add(s);
        ruleDlg.setSize(200, 400);
        ruleDlg.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        ruleDlg.setTitle("All Rules");
        ruleDlg.show();
    }

    JDialog ruleDlg = new JDialog( (JFrame)null, false);

    class MyListSelectionListener
        implements ListSelectionListener
    {
        public void valueChanged(ListSelectionEvent evt)
        {
            if (!evt.getValueIsAdjusting())
            {
                JList list = (JList) evt.getSource();

                // Get all selected items
                Object[] selected = list.getSelectedValues();
                if (selected != null && selected.length > 0)
                {
                    String term = (String) selected[0];
                    //  1. update relevant rules
                    lstRule.setListData(reasoner.getApplicableRuleSet(term).
                                        toArray());

                    // 2. find the EC on the tree
                    EqualClass ec = reasoner.getEqualClass(term);
                    if (ec != null)
                    {
                        TreeNode node = ecTree.selectFirst(ec);
                        ecTree.expandNode(node);
                    }
                    // 3. update CC and ICC
                    CompClass cc = reasoner.getCompClass(term);
                    if (cc != null)
                    {
                        ccList.setListData(cc.compatible.toArray());
                        iccList.setListData(cc.incompatible.toArray());
                    }

                }
            }
        }
    }
}
