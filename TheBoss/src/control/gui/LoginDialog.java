package control.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.SocketException;
import java.rmi.UnknownHostException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import control.impl.BossUnit;

public class LoginDialog extends JDialog implements ActionListener{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    JFrame owner;
    JPanel loginPanel;
    JTextField usernameTF, addressTF;
    JButton loginBtn, quitBtn;

    BossUnit bossUnit;
    
    public LoginDialog(JFrame owner, BossUnit bossUnit) {
        
        super(owner, true);
        
        this.owner = owner;
        this.bossUnit = bossUnit;
        
        loginPanel = new JPanel(new GridLayout(3, 2));
        loginPanel.setOpaque(true);
        loginPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        
        usernameTF = new JTextField("Guest", 20);
        addressTF = new JTextField("localhost", 30);
            
        loginBtn = new JButton("Login");
        // First time will fail
        loginBtn.registerKeyboardAction(
                loginBtn.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false)),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), 
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        loginBtn.registerKeyboardAction(
                loginBtn.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true)),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true), 
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        quitBtn = new JButton("Quit");
        
        loginBtn.addActionListener(this);
        quitBtn.addActionListener(this);
        
        loginPanel.add(new JLabel("Username:", SwingConstants.CENTER));
        loginPanel.add(usernameTF);
        loginPanel.add(new JLabel("Ip/Hostname:", SwingConstants.CENTER));
        loginPanel.add(addressTF);
        loginPanel.add(loginBtn);
        loginPanel.add(quitBtn);
        
        this.setContentPane(loginPanel);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                System.exit(0);
            }
        });
    }
    
    public void actionPerformed(ActionEvent event) {
        
        if (event.getActionCommand().equals("Login")) {
            this.setVisible(false);
            String hostname = addressTF.getText();
            String username = usernameTF.getText();
            try {
                bossUnit.login(hostname, username);
            } catch (IOException ioe) {
                if (ioe instanceof SocketException || ioe instanceof UnknownHostException) {
                    JOptionPane.showMessageDialog(this, "Server connect fail", "Caution!!", JOptionPane.ERROR_MESSAGE);
                    this.setVisible(true);
                } else {
                    ioe.printStackTrace();
                    System.exit(0);
                }
            }
                
        } else if(event.getActionCommand().equals("Quit")) {
            System.exit(0);
        }
    }
}
