package control.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import model.CardAction;
import model.impl.city.Chicago;
import control.impl.CityWinner;

public class LastUnveilDialog extends JDialog{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    JFrame owner;
    
    public LastUnveilDialog(JFrame owner, CardAction[] actions, CityWinner[] winners, int protectCityIndex) {
        
        super(owner, true);
        
        this.owner = owner;
        
        JPanel winnerPane = new JPanel(new GridLayout(2, actions.length + 1));
        winnerPane.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        
        int w = BossJFrame.unveilCard_width;
        int h = BossJFrame.unveilCard_height;
        for (int i = 0; i < actions.length; i++) {
            
            JLabel l = new JLabel();
            l.setIcon(BossJFrame.getActionImgIcon(actions[i], w, h));
            winnerPane.add(l);
            
            if (i == protectCityIndex) {
                JLabel pl = new JLabel();
                pl.setIcon(BossJFrame.getCityImgIcon(new Chicago(), w, h, false));
                winnerPane.add(pl);
            }
        }
        
        int j = 0;
        for (int i = 0; i < actions.length; i++) {
            
            winnerPane.add(new JLabel(winners[j++].getName(), SwingConstants.CENTER));
            
            if (i == protectCityIndex) {
                winnerPane.add(new JLabel(winners[j++].getName(), SwingConstants.CENTER));
            }
        }
        
        JButton okBtn = new JButton("Next hand");
        okBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                LastUnveilDialog.this.dispose();
            }            
        });
        
        JPanel pane = new JPanel(new BorderLayout());
        pane.add(winnerPane, BorderLayout.CENTER);
        pane.add(okBtn, BorderLayout.PAGE_END);
        
        this.setContentPane(pane);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
}
