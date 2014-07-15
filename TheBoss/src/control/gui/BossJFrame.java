package control.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import model.CardAction;
import model.City;
import model.TheBossBoardGame;
import model.constant.CITY_ID;
import model.constant.GANGSTER_TYPE;
import model.impl.Boss;
import model.impl.action.Reward;
import system.SystemParameter;
import control.impl.BossUnit;
import control.impl.CityWinner;
import control.impl.Snapshot;

public class BossJFrame extends JFrame {
  
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    static final int      cityCard_width = 132;
    static final int     cityCard_height = 180;
    static final int    unveilCard_width = 66;
    static final int   unveilCard_height = 90;
    static final int      userCard_width = 110;
    static final int     userCard_height = 150;
    static final int     enemyCard_width = 66;
    static final int    enemyCard_height = 45;
    static final int  gangsterCard_width = 22;
    static final int gangsterCard_height = 22;
        
    private JPanel contentPane, pageEndPane;
    private Snapshot[] snapshots;
    private int myId;
    private int numOfCityCards, numOfHandCards;
    private int numOfBosses;
    private int protectorIndex;
    
    public BossJFrame() {
    
        numOfHandCards = TheBossBoardGame.NUM_TURNS_IN_A_HAND_PER_BOSS;
        protectorIndex = -1;
        
        contentPane = new JPanel(new BorderLayout());
        pageEndPane = new JPanel(new BorderLayout());
        contentPane.add(pageEndPane, BorderLayout.PAGE_END);
        this.setContentPane(contentPane);
        
        /* fix UI part */
        /* mainPane is set up after num of bosses is determined */ // top-left
        /* OperationPane is set up after user-id is determined */   // top-right
        setupUserCardPane();    // bottom-left
        /* enemyCatdPane is set up after num of bosses is determined */   // bottom-right
    }
    
    public static ImageIcon getCityImgIcon(City c, int width, int height, boolean isBack) {

        StringBuilder imgPath = new StringBuilder();
        
        imgPath.append(SystemParameter.cardImgFolder)
               .append(c.getClass().getSimpleName().toLowerCase());
        if (isBack) {
            imgPath.append("_back");
        }
        imgPath.append(SystemParameter.cardImgType);
        
        ImageIcon icon = new ImageIcon(imgPath.toString());
        Image scaledImg = icon.getImage()
                              .getScaledInstance(
                                      width, 
                                      height,
                                      Image.SCALE_SMOOTH);        
        
        return new ImageIcon(scaledImg);
    }
    
    private ImageIcon getGangsterImgIcon(GANGSTER_TYPE type, int uid, int width, int height) {

        StringBuilder imgPath = new StringBuilder();
        imgPath.append(SystemParameter.cardImgFolder)
               .append(type.toString().toLowerCase())
               .append("_")
               .append(uid)
               .append(SystemParameter.cardImgType);
        
        ImageIcon icon = new ImageIcon(imgPath.toString());
        Image scaledImg = icon.getImage()
                              .getScaledInstance(
                                      width, 
                                      height,
                                      Image.SCALE_SMOOTH);        
        
        return new ImageIcon(scaledImg);
    }
    
    public static ImageIcon getActionImgIcon(CardAction a, int width, int height) {

        City c = TheBossBoardGame.CITIES[a.getCid().ordinal()];
        StringBuilder imgPath = new StringBuilder();
        imgPath.append(SystemParameter.cardImgFolder)
               .append(c.getClass().getSimpleName().toLowerCase())
               .append("_")
               .append(a.getClass().getSimpleName().toLowerCase());
        
        if (a instanceof Reward) {
            Reward reward = (Reward) a;
            imgPath.append("_")
                   .append(reward.getValue());
        }
        imgPath.append(SystemParameter.cardImgType);
        
        ImageIcon icon = new ImageIcon(imgPath.toString());
        Image scaledImg = icon.getImage()
                              .getScaledInstance(
                                      width, 
                                      height,
                                      Image.SCALE_SMOOTH);        
        
        return new ImageIcon(scaledImg);
    }

    /* top-left */
    private JRadioButton[] cityRButtons;  /* For user to choose city to occupy (per city) */
    private JPanel[] unveilPanes;         /* Show cards unveiled (per city) */
    private JPanel[] occupyPanes;         /* Show gangsters of each user (per city) */
    private JLabel[] cityCardLabels;      /* Show city cards */
    private ButtonGroup cityBtnGroup;
    
    public ButtonGroup getCityBtnGroup() { return cityBtnGroup; }
    
    private void setupMainPane(int numOfCityCards, int numOfBosses) {
        
        JPanel mainPane = new JPanel(new GridLayout(1, 0));
        Border mainPaneBorder = BorderFactory.createCompoundBorder(
                                    BorderFactory.createEmptyBorder(10, 10, 10, 10),
                                    BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        mainPane.setBorder(mainPaneBorder);
        
        cityBtnGroup = new ButtonGroup();        
        cityRButtons = new JRadioButton[numOfCityCards];
        cityCardLabels = new JLabel[numOfCityCards];
        unveilPanes = new JPanel[numOfCityCards];
        occupyPanes = new JPanel[numOfCityCards];
        
        /* add dummy labels to let mainPane always have 
           MAX_CITY_NUM components for UI alignment */        
        int MAX_CITY_NUM = TheBossBoardGame.CITIES.length + 1;
        int prePaddingNum = (MAX_CITY_NUM - numOfCityCards) / 2;
        int postPaddingNum = MAX_CITY_NUM - numOfCityCards - prePaddingNum;
        
        for (int i = 0; i < prePaddingNum; i++)
            mainPane.add(new JLabel());
        
        for (int i = 0; i < numOfCityCards; i ++) {
            
            cityRButtons[i] = new JRadioButton();
            cityRButtons[i].setAlignmentX(Component.CENTER_ALIGNMENT);
            cityRButtons[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    
                    Enumeration<AbstractButton> enumerator = cityBtnGroup.getElements();
                    int cid = 0;
                    while (enumerator.hasMoreElements()) {
                        JRadioButton btn = (JRadioButton) enumerator.nextElement();
                        if (btn.isSelected())
                            break;
                        cid++;
                    }
                    
                    Snapshot s = snapshots[myId];
                    int expertsInHand = s.getExpertsInHand();
                    int occasionalsInHand = s.getOccasionalsInHand();
                    
                    if (s.getExpertsInCity(cid) == 0)
                        occasionalsInHand = 0;
                    
                    setOperationPaneEnabled(expertsInHand, occasionalsInHand, true);
                } 
            });
            cityBtnGroup.add(cityRButtons[i]);
            
            cityCardLabels[i] = new JLabel();
            cityCardLabels[i].setPreferredSize(
                                new Dimension(
                                        cityCard_width,
                                        cityCard_height));
            cityCardLabels[i].setAlignmentX(Component.CENTER_ALIGNMENT);
            
            unveilPanes[i] = new JPanel(new GridLayout(0, cityCard_width / unveilCard_width));
            Dimension unveilPaneDim = new Dimension(cityCard_width, unveilCard_height * 2);
            unveilPanes[i].setPreferredSize(unveilPaneDim);
            unveilPanes[i].setMinimumSize(unveilPaneDim);
            unveilPanes[i].setMaximumSize(unveilPaneDim);
            unveilPanes[i].setAlignmentX(Component.CENTER_ALIGNMENT);
            
            occupyPanes[i] = new JPanel(new GridLayout(0, cityCard_width / gangsterCard_width));
            Dimension occupyPaneDim = new Dimension(cityCard_width, gangsterCard_height * numOfBosses);
            occupyPanes[i].setPreferredSize(occupyPaneDim);
            occupyPanes[i].setMinimumSize(occupyPaneDim);
            occupyPanes[i].setMaximumSize(occupyPaneDim);
            occupyPanes[i].setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.add(cityRButtons[i]);
            p.add(cityCardLabels[i]);
            p.add(Box.createRigidArea(new Dimension(0, 5)));
            p.add(unveilPanes[i]);
            p.add(Box.createRigidArea(new Dimension(0, 5)));
            p.add(occupyPanes[i]);
            p.add(new JLabel(" ")); /* dummy label for alignment */
            
            mainPane.add(p);
        }

        for (int i = 0; i < postPaddingNum; i++)
            mainPane.add(new JLabel());
        
        contentPane.add(mainPane, BorderLayout.CENTER);
    }
    
    public void nextHandMainPane() {

        cityBtnGroup.clearSelection();
        
        for (JPanel p: unveilPanes) {
            p.removeAll();
            p.updateUI();
        }
        
        for (JPanel p: occupyPanes) {
            p.removeAll();
            p.updateUI();
        }
    }
    
    public void setMainPaneEnabled(boolean b) {
        
        Enumeration<AbstractButton> enumerator = cityBtnGroup.getElements(); 
        int i = 0;
        while(enumerator.hasMoreElements()) {     
            JRadioButton btn = (JRadioButton) enumerator.nextElement();       
            if (i == protectorIndex && snapshots[myId].getProtectEnabled() == false) {
                /* keep disabled */
                btn.setEnabled(false);
            } else {
                btn.setEnabled(b);
            }
            i++;
        }
        cityBtnGroup.clearSelection();
        
        passBtn.setEnabled(b);
    }
    
    public void showCityCards(int protectCityIndex) {
    
        int j = 0;
        int protectorIndex = protectCityIndex + 1;
        
        this.protectorIndex = protectorIndex;
        
        for (int i = 0; i < numOfCityCards; i++) {
            
            City city = (i == protectorIndex) ?
                        (City)TheBossBoardGame.PROTECTOR :
                        TheBossBoardGame.CITIES[j++];           
            
            cityCardLabels[i].setIcon(getCityImgIcon(
                                        city,
                                        cityCard_width,
                                        cityCard_height,
                                        false));
        }
    }
    
    public void unveilActionCard(CardAction action) {
        
        int unveilCityIndex = action.getCid().ordinal();
        if (unveilCityIndex >= protectorIndex) // in BossJFrame, protector is one of cities
            unveilCityIndex++;
        
        JLabel label = new JLabel();
        label.setIcon(getActionImgIcon(action, unveilCard_width, unveilCard_height));
        unveilPanes[unveilCityIndex].add(label);
        unveilPanes[unveilCityIndex].validate();
    }
    
    public void updateGangsters(int cid) {

        //int cityId = cid;
        //if (cityId >= protectorIndex) // in BossJFrame, protector is one of cities
        //    cityId++;
        
        JPanel pane = occupyPanes[cid];
        pane.removeAll();
        
        for (int id = 0; id < snapshots.length; id++) {
            for (int j = 0; j < snapshots[id].getExpertsInCity(cid); j++) {
                JLabel label = new JLabel();
                label.setIcon(getGangsterImgIcon(
                                GANGSTER_TYPE.EXPERT,
                                id,
                                gangsterCard_width,
                                gangsterCard_height));
                pane.add(label);
            }
            for (int j = 0; j < snapshots[id].getOccasionalsInCity(cid); j++) {
                JLabel label = new JLabel();
                label.setIcon(getGangsterImgIcon(
                                GANGSTER_TYPE.OCCASIONAL,
                                id,
                                gangsterCard_width,
                                gangsterCard_height));
                pane.add(label);
            }
        }
        
        pane.updateUI();
    }
    
    /* For protector */
    /*public void updateGangsters() {

        JPanel pane = occupyPanes[protectorIndex];
        pane.removeAll();
        
        for (int id = 0; id < snapshots.length; id++) {
            for (int j = 0; j < snapshots[id].getExpertsInProtector(); j++) {
                JLabel label = new JLabel();
                label.setIcon(getGangsterImgIcon(
                                GANGSTER_TYPE.EXPERT,
                                id,
                                gangsterCard_width,
                                gangsterCard_height));
                pane.add(label);
            }
            for (int j = 0; j < snapshots[id].getOccasionalsInProtector(); j++) {
                JLabel label = new JLabel();
                label.setIcon(getGangsterImgIcon(
                                GANGSTER_TYPE.OCCASIONAL,
                                id,
                                gangsterCard_width,
                                gangsterCard_height));
                pane.add(label);
            }
        }
        
        pane.updateUI();
    }*/
    
    /* top-right */
    /* For user putting gangsters */
    private JComboBox<String> expertCB, occasionalCB;
    private JButton execBtn, passBtn;
    private JPanel prisonDay1Pane, prisonDay2Pane, hospitalPane;
    
    public JComboBox<String> getExpertCB() { return expertCB; }
    public JComboBox<String> getOccasionalCB() { return occasionalCB; }
    public JButton getExecBtn() { return execBtn; }
    public JButton getPassBtn() { return passBtn; }
    
    private void setupOperationPane() {
        
        JPanel operationPane = new JPanel();
        operationPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));
        operationPane.setLayout(new BoxLayout(operationPane, BoxLayout.Y_AXIS));

        int maxExperts = Boss.MAX_NUM_OF_EXPERTS;
        int w = maxExperts * gangsterCard_width;
        int h = numOfBosses * gangsterCard_height;
        Dimension d = new Dimension(w, h);
        
        JLabel tipsLabel = new JLabel("In prison (day-1):");
        tipsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        operationPane.add(tipsLabel);        
        prisonDay1Pane = new JPanel(new GridLayout(0, maxExperts));
        prisonDay1Pane.setPreferredSize(d);
        prisonDay1Pane.setMinimumSize(d);
        prisonDay1Pane.setMaximumSize(d);
        prisonDay1Pane.setAlignmentX(Component.LEFT_ALIGNMENT);
        operationPane.add(prisonDay1Pane);

        tipsLabel = new JLabel("In prison (day-2):");
        tipsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        operationPane.add(tipsLabel);
        prisonDay2Pane = new JPanel(new GridLayout(0, maxExperts));
        prisonDay2Pane.setPreferredSize(d);
        prisonDay2Pane.setMinimumSize(d);
        prisonDay2Pane.setMaximumSize(d);
        prisonDay2Pane.setAlignmentX(Component.LEFT_ALIGNMENT);
        operationPane.add(prisonDay2Pane);

        tipsLabel = new JLabel("In hospital:");
        operationPane.add(tipsLabel);
        tipsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);        
        hospitalPane = new JPanel(new GridLayout(0, maxExperts));
        hospitalPane.setPreferredSize(d);
        hospitalPane.setMinimumSize(d);
        hospitalPane.setMaximumSize(d);
        hospitalPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        operationPane.add(hospitalPane);
        operationPane.add(new JSeparator(JSeparator.HORIZONTAL));
        
        ActionListener cbAction = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                
                if (expertCB.getSelectedIndex() == expertCB.getItemCount() - 1 && 
                    occasionalCB.getSelectedIndex() == occasionalCB.getItemCount() - 1)
                    execBtn.setEnabled(false);
                else
                    execBtn.setEnabled(true);
            }            
        };
        
        expertCB = new JComboBox<String>();
        expertCB.setMaximumSize(new Dimension(120, 10));
        expertCB.setAlignmentX(Component.LEFT_ALIGNMENT);
        expertCB.addItem(Integer.toString(Boss.MAX_NUM_OF_EXPERTS));
        expertCB.addActionListener(cbAction);

        occasionalCB = new JComboBox<String>();
        occasionalCB.setMaximumSize(new Dimension(120, 10));
        occasionalCB.setAlignmentX(Component.LEFT_ALIGNMENT);
        occasionalCB.addItem(Integer.toString(Boss.MAX_NUM_OF_OCCASONALS));
        occasionalCB.addActionListener(cbAction);

        execBtn = new JButton("Execute");
        passBtn = new JButton("Pass");
        
        tipsLabel = new JLabel("Experts you have:",
                                      getGangsterImgIcon(
                                              GANGSTER_TYPE.EXPERT,
                                              myId,
                                              gangsterCard_width,
                                              gangsterCard_height),
                                      JLabel.LEADING);
        tipsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        operationPane.add(tipsLabel);
        operationPane.add(expertCB);
        
        tipsLabel = new JLabel("Occasionals you have:",
                               getGangsterImgIcon(
                                       GANGSTER_TYPE.OCCASIONAL,
                                       myId,
                                       gangsterCard_width,
                                       gangsterCard_height),
                               JLabel.LEADING);
        tipsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        operationPane.add(tipsLabel);
        operationPane.add(occasionalCB);
        
        JPanel btnPanel = new JPanel(new GridLayout(1, 2));
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnPanel.add(execBtn);
        btnPanel.add(passBtn);
        operationPane.add(Box.createRigidArea(new Dimension(0, 10)));
        operationPane.add(btnPanel);
        
        contentPane.add(operationPane, BorderLayout.LINE_END);
    }
    
    public void nextHandOperationPane() {

        expertCB.removeAllItems();
        expertCB.addItem(Integer.toString(snapshots[myId].getExpertsInHand()));
        expertCB.validate();

        /* occasionals cannot be reused */
        occasionalCB.removeAllItems();
        occasionalCB.addItem(Integer.toString(snapshots[myId].getOccasionalsInHand()));
        occasionalCB.validate();
    }

    public void setOperationPaneEnabled(boolean b) {

        expertCB.setEnabled(b);
        occasionalCB.setEnabled(b);
        execBtn.setEnabled(b);
        passBtn.setEnabled(b);
    }
    
    public void setOperationPaneEnabled(int expertsInHand, int occasionalsInHand, boolean b) {
        
        expertCB.removeAllItems();
        for (int i = expertsInHand; i >= 0; i--) {
            expertCB.addItem(Integer.toString(i));
        }
        expertCB.validate();
        expertCB.setSelectedIndex(0);
        
        occasionalCB.removeAllItems();
        for (int i = occasionalsInHand; i >= 0; i--) {
            occasionalCB.addItem(Integer.toString(i));
        }
        occasionalCB.validate();
        occasionalCB.setSelectedIndex(0);
        
        /* if no gangster in hand, always set enabled to false. */
        if (expertsInHand == 0)
            expertCB.setEnabled(false);
        else
            expertCB.setEnabled(b);
        
        if (occasionalsInHand == 0)
            occasionalCB.setEnabled(false);
        else
            occasionalCB.setEnabled(b);
        
        if (expertsInHand == 0 && occasionalsInHand == 0)
            execBtn.setEnabled(false);
        else
            execBtn.setEnabled(b);
        /* execBtn will be enabled after some experts/occasionals selected */ 
        
        passBtn.setEnabled(true);
    }
    
    public void showImprisonAndHospitalize() {

        prisonDay1Pane.removeAll();
        prisonDay1Pane.updateUI();
        prisonDay2Pane.removeAll();
        prisonDay2Pane.updateUI();
        hospitalPane.removeAll();
        hospitalPane.updateUI();
        
        for (int i = 0; i < snapshots.length; i++)
            showImprisonAndHospitalize(snapshots[i]);
    }
    
    private void showImprisonAndHospitalize(Snapshot snapshot) {
    
        int pDay1 = snapshot.getExpertsInPrisonDay1();
        int pDay2 = snapshot.getExpertsInPrisonDay2();
        int hosp = snapshot.getExpertsInHospital();
        
        for (int i = 0; i < pDay1; i++) {
            JLabel label = new JLabel();
            label.setIcon(getGangsterImgIcon(
                            GANGSTER_TYPE.EXPERT,
                            snapshot.getID(),
                            gangsterCard_width,
                            gangsterCard_height));
            prisonDay1Pane.add(label);
        }
        
        for (int i = 0; i < pDay2; i++) {
            JLabel label = new JLabel();
            label.setIcon(getGangsterImgIcon(
                            GANGSTER_TYPE.EXPERT,
                            snapshot.getID(),
                            gangsterCard_width,
                            gangsterCard_height));
            prisonDay2Pane.add(label);
        }

        for (int i = 0; i < hosp; i++) {
            JLabel label = new JLabel();
            label.setIcon(getGangsterImgIcon(
                            GANGSTER_TYPE.EXPERT,
                            snapshot.getID(),
                            gangsterCard_width,
                            gangsterCard_height));
            hospitalPane.add(label);
        }
    }
    
    /* bottom-left */
    /* Show card back of other users (per user) */
    private Map<Integer, EnemyCardSet> cardBackMap;
    private List<EnemyCardSet> enemyCardSets;
    
    static class CityLabel extends JLabel {
        
        private static final long serialVersionUID = 1L;        
        CITY_ID cid;
        
        public CityLabel(CITY_ID cid) {

            this.cid = cid;
        }
    }
    
    static class EnemyCardSet {
        
        JLabel nameLabel;
        JPanel cardBackPane;
        
        EnemyCardSet(JLabel nameLabel, JPanel cardBackPane) {
            
            this.nameLabel = nameLabel;
            this.cardBackPane = cardBackPane;
        }
    }
    
    private void setupEnemyCardPane(int numOfEnemy) {

        JPanel cardBackPane = new JPanel();
        Border cardBackPaneBorder = BorderFactory.createCompoundBorder(
                                        BorderFactory.createEmptyBorder(0, 10, 10, 10),
                                        BorderFactory.createRaisedBevelBorder());
        cardBackPane.setBorder(cardBackPaneBorder);
        cardBackPane.setLayout(new BoxLayout(cardBackPane, BoxLayout.Y_AXIS));  
        
        cardBackMap = new HashMap<Integer, EnemyCardSet>(numOfEnemy);
        enemyCardSets = new ArrayList<EnemyCardSet>();
        
        for (int i= 0; i < numOfEnemy; i++) {
            
            EnemyCardSet set = new EnemyCardSet(
                                    new JLabel(),
                                    new JPanel(new GridLayout(1, 5, 5, 10)));
            set.nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            cardBackPane.add(set.nameLabel);

            set.cardBackPane.setAlignmentX(Component.LEFT_ALIGNMENT);
            set.cardBackPane.setPreferredSize(new Dimension(
                                    (enemyCard_width + 5) * numOfHandCards,
                                    enemyCard_height + 10));
            cardBackPane.add(set.cardBackPane);            
            enemyCardSets.add(set);
        }     
        pageEndPane.add(cardBackPane, BorderLayout.LINE_START);
    }

    public void nextHandEnemyCardPane() {
        
        for (EnemyCardSet set: enemyCardSets) {
            set.cardBackPane.removeAll();
            set.cardBackPane.validate();
        }        
    }
    
    private JLabel showEnemyInfo(Snapshot snapshot) {
        
        int id = snapshot.getID();
        JLabel nameLabel = cardBackMap.get(id).nameLabel;
        nameLabel.setText(snapshot.getBossName() + 
                          " - Reward$" + snapshot.getReward());
        nameLabel.setIcon(getGangsterImgIcon(
                              GANGSTER_TYPE.OCCASIONAL,
                              id,
                              gangsterCard_width,
                              gangsterCard_height));
        nameLabel.setAlignmentY(JLabel.LEADING);
        
        return nameLabel;
    }
    
    private JLabel showEnemyInfo(Snapshot snapshot, String text) {
        
        JLabel nameLabel = showEnemyInfo(snapshot);
        nameLabel.setText(nameLabel.getText() + " " + text);
        
        return nameLabel;
    }
    
    public void removeEnemyCards(int id, CITY_ID cid) {
        
        JPanel pane = cardBackMap.get(id).cardBackPane;
        for (Component c: pane.getComponents()) {
            CityLabel cityLabel = (CityLabel) c;
            if (cityLabel.cid == cid) {                
                pane.remove(c);
                pane.add(new JLabel()); /* add an empty label for card alignment */
                pane.validate();
                break;
            }
        }
    }
    
    public void showEnemyCards(int id, CITY_ID[] cids) {
        
        EnemyCardSet set = enemyCardSets.remove(0);

        JPanel pane = set.cardBackPane;
        for (CITY_ID cid: cids) {
            City city = TheBossBoardGame.CITIES[cid.ordinal()];
            CityLabel cityLabel = new CityLabel(cid);
            cityLabel.setIcon(getCityImgIcon(
                                city,
                                enemyCard_width,
                                enemyCard_height,
                                true));
            pane.add(cityLabel);
        }
        enemyCardSets.add(set);
        
        if (!cardBackMap.containsKey(id))
            cardBackMap.put(id, set);
        else
            if (cardBackMap.get(id) != set)
                throw new RuntimeException();
        
        pane.validate();
    }
    
    /* bottom-right */
    private List<ActionLabel> userCardLabels;
    private JPanel userCardPane;    /* Show cards dispatched to the user */
    private JLabel userInfoLabel;
    private boolean userCardPaneEnabled;
    
    public ActionLabel[] getUserCardLabels() {
        
        return userCardLabels.toArray(new ActionLabel[userCardLabels.size()]);
    }
    
    public static class ActionLabel extends JLabel {
    
        private static final long serialVersionUID = 1L;
        public CardAction action;
        
        public ActionLabel(CardAction action) {
            
            this.action = action;
        }
    }
    
    private void setupUserCardPane() {
        
        int cardNum = TheBossBoardGame.NUM_TURNS_IN_A_HAND_PER_BOSS;
        userInfoLabel = new JLabel();
        userCardLabels = new ArrayList<ActionLabel>();
                
        JPanel p = new JPanel(new GridLayout(1, cardNum, 10, 0));
        for (int i = 0; i < cardNum; i++) {
            ActionLabel l = new ActionLabel(null);
            l.setPreferredSize(
                    new Dimension(userCard_width, userCard_height));
            userCardLabels.add(l);
            p.add(l);
        }

        userCardPane = new JPanel(new BorderLayout());
        userCardPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));
        userCardPane.add(userInfoLabel, BorderLayout.PAGE_START);
        userCardPane.add(p, BorderLayout.CENTER);
        pageEndPane.add(userCardPane, BorderLayout.CENTER);
    }
    
    private void showMyInfo(Snapshot snapshot) {
        
        userInfoLabel.setText(snapshot.getBossName() + 
                              " - Reward$" + snapshot.getReward());
    }
    
    private void showMyInfo(Snapshot snapshot, String text) {
        
        userInfoLabel.setText(snapshot.getBossName() + 
                              " - Reward$" + snapshot.getReward() + 
                              " " + text);
    }
    
    public void showUserCards(CardAction[] actions) {
        
        for (CardAction a: actions) {
            
            ActionLabel label = userCardLabels.remove(0);
            label.setIcon(getActionImgIcon(a, userCard_width, userCard_height));
            label.action = a;
            
            userCardLabels.add(label);
        }
        userCardPane.validate();
    }
    
    public boolean isUserCardPaneEnabled() { return userCardPaneEnabled; }
    public void setUserCardPaneEnabled(boolean b) { userCardPaneEnabled = b; }
    
    public void showUserInfo(int id) {
    
        if (id == myId)
            this.showMyInfo(snapshots[id]);
        else
            this.showEnemyInfo(snapshots[id]);
    }

    public void showUserInfo(int id, String text) {
    
        if (id == myId)
            this.showMyInfo(snapshots[id], text);
        else
            this.showEnemyInfo(snapshots[id], text);
    }
    
    /* shall only called once */
    public void init(Snapshot[] snapshots, int numOfBosses, int myId) {
        
        numOfCityCards = 2 * numOfBosses;
        this.numOfBosses = numOfBosses;
        this.snapshots = snapshots;
        this.myId = myId;
            
        setupMainPane(numOfCityCards, numOfBosses);
        setupOperationPane();
        setupEnemyCardPane(numOfBosses - 1);
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    public void showLastUnveilDialog(CardAction[] actions, CityWinner[] winners) {
        
        JDialog d = new LastUnveilDialog(this, actions, winners, protectorIndex - 1);
        d.setTitle("The result of this hand:");
        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }
    
    public static void start() {
        
        
        JFrame f = new BossJFrame();
        //f.setResizable(false);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(false);
        
        // Main controller
        BossUnit bossUnit = new BossUnit((BossJFrame)f);
        
        JDialog d = new LoginDialog(f, bossUnit);
        d.setTitle("Please login");
        d.setSize(350, 160);
        d.setLocationRelativeTo(f);
        d.setVisible(true);
    }
    
    public static void main(String[] args) {
        
        SwingUtilities.invokeLater(new Runnable() {
           public void run() {
               BossJFrame.start();
           }
        });
    }

}
