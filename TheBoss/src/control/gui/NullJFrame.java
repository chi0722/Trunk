package control.gui;

import model.CardAction;
import model.constant.CITY_ID;

public class NullJFrame extends BossJFrame {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public void init(int numOfBosses) {}
    public void showCityCards(int protectCityIndex) {}
    public void showEnemyCards(String name, CITY_ID[] cids) {}
    public void showUserCards(CardAction[] cards) {}
    public void setOperationPaneEnabled(boolean b) {}
    public void setMainPaneEnabled(boolean b) {}
    public void setVisible(boolean b) {} /* never shown */
}
