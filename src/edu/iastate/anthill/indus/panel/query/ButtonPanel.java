/**
 * Copyright by Cleverlance 2001
 * Contact: development@cleverlance.com
 * Website: www.cleverlance.com
 */

package edu.iastate.anthill.indus.panel.query;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Class with panel of basic buttons.<BR>
 *
 * @author	Jan Seda
 * @version	0.3.07
 *
 * Changed by Jie Bao and Jyotish 2005-03-22
 */
public class ButtonPanel
    extends JPanel implements ActionListener
{
    /** Action command for next button. */
    private static final String NEXT_CMD = "0x1NX";
    /** Action command for previous button. */
    private static final String PREV_CMD = "0x2PR";
    /** Action command for cancel button. */
    private static final String CANC_CMD = "0x3CL";
    /** Action command for finish button. */
    private static final String FINS_CMD = "0x4FN";
    /** Action command for option button. */
    private static final String OPTN_CMD = "0x5OP";

    /** Counting current step. */
    private int _current = 0;
    /** Number of steps to show Finish button and to call finish method. */
    private int _stepCount;
    /** Parent pane with button's interface. */
    private SQLBuilderPane _parent;

    /** Button for options on the begining of the builder. */
    public JButton options = new JButton();
    /** Back-move button. */
    public JButton back = new JButton();
    /** Next-move button. */
    public JButton next = new JButton();
    /** Calcel button - fires closePanel event. */
    public JButton cancel = new JButton();
    /** Finish button. */
    public JButton finish = new JButton();

    /** Space between buttons. */
    private JPanel space1 = new JPanel();

    /**
     * Class constructor.
     *
     *@param		<B>parent</B> parent pane owning buttons
     *@param		<B>stepCount</B> number of steps to use builder buttons
     */
    public ButtonPanel( SQLBuilderPane parent, int stepCount)
    {
        // set parent pane for buttons
        _parent = parent;
        // reinitialize counter for internal couting
        _stepCount = stepCount;

        // setup action commands
        cancel.setActionCommand(this.CANC_CMD);
        cancel.addActionListener(this);
        next.setActionCommand(this.NEXT_CMD);
        next.setMnemonic(KeyEvent.VK_RIGHT);
        next.addActionListener(this);
        back.setActionCommand(this.PREV_CMD);
        back.setMnemonic(KeyEvent.VK_LEFT);
        back.addActionListener(this);
        options.setActionCommand(this.OPTN_CMD);
        options.addActionListener(this);

        // Bao - hide the option button , since it's not used in this INDUS demo
        // 2005-03-23
        options.setVisible(false);

        finish.setActionCommand(this.FINS_CMD);
        finish.addActionListener(this);

        setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints1;

        options.setText("Options...");

        options.setToolTipText("Options for table types.");

        gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.fill = GridBagConstraints.BOTH;
        add(options, gridBagConstraints1);

        gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 2;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.fill = GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new Insets(0, 26, 0, 26);
        add(space1, gridBagConstraints1);

        back.setText("<Back");
        back.setEnabled(false);
            back.setToolTipText("Move to previous dialog.");

        gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 3;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.fill = GridBagConstraints.BOTH;
        add(back, gridBagConstraints1);

        next.setText("Next>");
        next.setEnabled(false);
            next.setToolTipText("Move to next dialog.");

        gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 4;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.fill = GridBagConstraints.BOTH;
        add(next, gridBagConstraints1);

        finish.setText("Finish");

            next.setToolTipText("Finish query wizard.");

        finish.setEnabled(true);
        finish.setVisible(false);

            next.setToolTipText("Move to next dialog.");

        gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 4;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.fill = GridBagConstraints.BOTH;
        add(finish, gridBagConstraints1);

        cancel.setText("Cancel");
            cancel.setToolTipText("Close query builder.");

        gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 5;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.fill = GridBagConstraints.BOTH;
        add(cancel, gridBagConstraints1);
    }

    public void actionPerformed(ActionEvent e)
    {
        String cmd = e.getActionCommand();
        if (cmd.equals(this.NEXT_CMD))
        {
            _current += 1;
            back.setEnabled(true);
            if (_current == _stepCount)
            {
                next.setEnabled(false);
            }

            // initalize each pane appropriately
            switch (_current)
            {
                case 1:

                    // second pane
                    //System.out.println(_parent.generateZQuery());

                    //_parent.sqlDlg.setSQL(_parent.generateZQuery().toString());
                    _parent.secondPane.update(_parent.firstPane.listModel);
                    break;
                    //case 2:

                    // third pane
                    //     _parent.thirdPane.init(_parent.firstPane.listModel);
                    //     break;
            }

            _parent.next(_current);
        }
        else if (cmd.equals(this.PREV_CMD))
        {
            _current -= 1;
            next.setEnabled(true);
            if (_current == 0)
            {
                back.setEnabled(false);
            }
            _parent.previous(_current);
        }
        else if (cmd.equals(this.CANC_CMD))
        {
            _parent.cancel();
        }
        else if (cmd.equals(this.FINS_CMD))
        {
            _parent.finish();
        }
        else if (cmd.equals(this.OPTN_CMD))
        {
            _parent.firstPane.options();
        }

        if (_current > 0)
        {
            options.setEnabled(false);
        }
        else
        {
            options.setEnabled(true);
        }

        if (_current == (_stepCount - 1))
        {
            next.setVisible(false);
            finish.setVisible(true);
        }
        else if (finish.isVisible())
        {
            next.setVisible(true);
            finish.setVisible(false);
        }
    }
}
