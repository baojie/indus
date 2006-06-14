package edu.iastate.anthill.indus.reasoner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.text.Position.Bias;

import edu.iastate.utils.gui.JTreeEx;
import edu.iastate.anthill.indus.datasource.mapping.SimpleConnector;
import javax.swing.DefaultListModel;

public class ReasonerPanelGUI
    extends JPanel
{
    ShortBridgeReasoner reasoner;

    public ReasonerPanelGUI(ShortBridgeReasoner reasoner)
    {
        super();
        try
        {
             this.reasoner = reasoner;
            jbInit();
        }
        catch (Exception ex)
        {
        }
    }

    JTreeEx ecTree = new JTreeEx();

    DefaultListModel lstTermModel = new DefaultListModel();
    JList lstTerm = new JList(lstTermModel);
    JList lstRule = new JList();
    JList ccList = new JList();
    JList iccList = new JList();

    JButton btnValidate = new JButton("Check Consistency");

    JScrollPane paneEC = new JScrollPane();
    JScrollPane jScrollPane1 = new JScrollPane();
    JScrollPane jScrollPane3 = new JScrollPane();

    JPanel paneRight = new JPanel();
    JPanel paneCC = new JPanel();
    JPanel paneICCList = new JPanel();
    JPanel paneCCList = new JPanel();
    JPanel paneButton = new JPanel();
    JPanel paneLeft = new JPanel();

    BorderLayout borderLayout1 = new BorderLayout();
    GridLayout gridLayout1 = new GridLayout();
    BorderLayout borderLayout2 = new BorderLayout();
    BorderLayout borderLayout3 = new BorderLayout();
    GridLayout gridLayout2 = new GridLayout();
    GridLayout gridLayout3 = new GridLayout();

    TitledBorder titledBorder1 = new TitledBorder("Compatible Class");
    TitledBorder titledBorder2 = new TitledBorder("Incompatible Class");
    TitledBorder titledBorder3 = new TitledBorder("Applicable Rules");
    TitledBorder titledBorder4 = new TitledBorder("Term List");
    JButton btnSubsumption = new JButton();
    JButton btnViewRule = new JButton();

    private void jbInit() throws Exception
    {

        this.setLayout(borderLayout1);

        // 1 . left pane
        paneLeft.setLayout(gridLayout3);
        gridLayout3.setColumns(1);
        gridLayout3.setRows(2);
        btnSubsumption.setText("Check Into");
        this.add(paneLeft, BorderLayout.WEST);

        // 1.1 left upper : term list
        lstTerm.setFixedCellWidth(200);
        jScrollPane1.setBorder(titledBorder4);
        jScrollPane1.getViewport().add(lstTerm);
        paneLeft.add(jScrollPane1);
        lstTerm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 1.2 left bottom : rule list
        jScrollPane3.getViewport().add(lstRule);
        jScrollPane3.setBorder(titledBorder3);
        paneLeft.add(jScrollPane3);
        lstRule.setCellRenderer(new RuleListCellRenderer());

        //  2 . right pane
        this.add(paneRight, BorderLayout.CENTER);
        paneRight.setLayout(gridLayout1);
        gridLayout1.setColumns(1);
        gridLayout1.setRows(2);

        // 2.1 right upper: the EC Forest
        paneRight.add(paneEC);

        // 2.2 right lower : the CC and ICC list
        paneCC.setLayout(gridLayout2);
        paneRight.add(paneCC);

        // 2.2.1 compatible class list
        paneCCList.setLayout(borderLayout2);
        paneCCList.add(ccList, BorderLayout.CENTER);
        paneCCList.setBorder(titledBorder1);
        paneCC.add(paneCCList, null);
        ccList.setCellRenderer(new CCListCellRenderer());

        // 2.2.2 incompatible class list
        paneICCList.setLayout(borderLayout3);
        paneICCList.add(iccList, BorderLayout.CENTER);
        paneICCList.setBorder(titledBorder2);
        paneCC.add(paneICCList, null);
        iccList.setCellRenderer(new CCListCellRenderer());

        // 3 button pane (bottom)
        this.add(paneButton, BorderLayout.SOUTH);
        btnViewRule.setText("View All Rules");
        paneButton.add(btnViewRule);
        paneButton.add(btnSubsumption);
        paneButton.add(btnValidate);
    }

    // 2005-04-12
    class CCListCellRenderer
        extends DefaultListCellRenderer
    {
        public Component getListCellRendererComponent(
            JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus)
        {
            Component retValue = super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
            //System.out.println(value);
            if (value != null && ccList.getModel().getSize() > 0 &&
                iccList.getModel().getSize() > 0)
            {
                // make the items in both cc and icc list red
                int indexCC = ccList.getNextMatch(value.toString(), 0,
                                                  Bias.Forward);
                int indexICC = iccList.getNextMatch(value.toString(), 0,
                    Bias.Forward);

                if (indexCC >= 0 && indexICC >= 0)
                {
                    //System.out.println(indexCC + "," + indexICC);
                    this.setForeground(Color.RED);
                }
            }
            return retValue;
        }
    }

    class RuleListCellRenderer
        extends DefaultListCellRenderer
    {
        public Component getListCellRendererComponent(
            JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus)
        {
            Component retValue = super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
            // if a rule is UNEQU and both terms are in the same EC, make it red
            ShortBridgeRule rule = (ShortBridgeRule) value;
            if (rule.connector.equals(SimpleConnector.UNEQU))
            {
                String t1 = rule.term1;
                EqualClass ec1 = reasoner.getEqualClass(t1);
                String t2 = rule.term2;
                EqualClass ec2 = reasoner.getEqualClass(t2);
                if (ec1 == ec2)
                {
                    this.setForeground(Color.RED);
                }
            }
            return retValue;
        }
    }
}
