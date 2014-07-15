package control.impl;

import io.MessageIO;
import io.MessageIOHandler;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;

import model.CardAction;
import model.TheBossBoardGame;
import model.constant.CITY_ID;
import util.Message;
import util.MessageAction;
import util.MessageActionEvent;
import util.MessageType;
import control.State;
import control.Unit;
import control.gui.BossJFrame;
import control.gui.BossJFrame.ActionLabel;

public class BossUnit extends Unit {

    private Thread eventThread;
    private MessageIO mioToBoard;
    private Snapshot[] snapshots;
    private CityWinner[] cityWinners;
    private Map<CITY_ID, List<CardAction>> actionsUnveiledMap;
    private BlockingQueue<MessageActionEvent> eventQ;
    private List<CardAction> actions;
    private String username;
    private int myId;
    private int numOfBosses;
    private int numOfBossesScored;
    private int numOfCities;
    private int protectCityIndex;

    // gui
    private final BossJFrame frame;

    public BossUnit(BossJFrame frame) {

        this.frame = frame;

        stateChange(initState);

        actions = new ArrayList<CardAction>();
        eventQ = new LinkedBlockingQueue<MessageActionEvent>();
        myId = -1;   
    }

    /* invoked in AWT thread, shall return soon */
    public void login(String hostname, String username) 
            throws UnknownHostException, IOException {

        this.username = username;

        Socket socket = new Socket(hostname, TheBossBoardGame.PORT);
        mioToBoard = new MessageIO(socket);

        eventThread = new Thread() {
        
            public void run() {
                
                MessageActionEvent event;
                try {
                    while ((event = eventQ.take()) != null) {
                        BossUnit.this.handleMessage(event);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    stateChange(endState);
                }
            }
        };
        eventThread.start();
        
        MessageIOHandler handler = new MessageIOHandler(mioToBoard, eventQ);
        handler.setMessageListener(this);
        handler.start();

        frame.setTitle(username + " - The Boss Board Game");
        registry();
    }

    @Override
    public void handleIOException(MessageIO src, IOException e) {

        JOptionPane.showMessageDialog(frame, "Someone leaves the game", "Caution!!", JOptionPane.ERROR_MESSAGE);
        eventThread.interrupt();
    }
    
    private void registry() throws IOException{

        mioToBoard.writeMessage(MessageType.REGISTER, username);
    }

    /* frame.init() shall be called before to construct all frame components. */
    private void initFrameActions() {

        // Main pane

        // Gangster control pane
        frame.getExecBtn().addActionListener(new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override
            public void actionPerformed(ActionEvent e) {

                cidToPutGangsters = -1;
                Enumeration<AbstractButton> enumerator = frame.getCityBtnGroup().getElements();
                int i = 0;
                while (enumerator.hasMoreElements()) {
                    JRadioButton btn = (JRadioButton) enumerator.nextElement();
                    if (btn.isSelected()) {
                        cidToPutGangsters = i;
                        break;
                    }
                    i++;
                }

                numOfExpertsToPut = Integer.valueOf(
                        (String)frame.getExpertCB().getSelectedItem());
                numOfOccasionalsToPut = Integer.valueOf(
                        (String)frame.getOccasionalCB().getSelectedItem());
                
                if (cidToPutGangsters != -1 &&
                    cityWinners[cidToPutGangsters].isCityWinner(
                            myId,
                            numOfExpertsToPut + numOfOccasionalsToPut) == false) {

                    JOptionPane.showMessageDialog(frame, "Less gangsters than current occupancy", "Wrong!!", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                frame.setOperationPaneEnabled(false);
                /* shall get selected status before un-select all buttons */
                frame.setMainPaneEnabled(false);
                
                /* user card choose, notify and unveil */
                synchronized(waitForTurnState) {
                    waitForTurnState.notifyAll();
                }
            }	        
        });

        frame.getPassBtn().addActionListener(new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override
            public void actionPerformed(ActionEvent e) {

                frame.setOperationPaneEnabled(false);
                frame.setMainPaneEnabled(false);

                cidToPutGangsters = -1;

                /* user card choose, notify and unveil */
                synchronized(waitForTurnState) {
                    waitForTurnState.notifyAll();
                }
            }           
        });

        // User card choose pane
        frame.showUserInfo(myId);
        ActionLabel[] labels = frame.getUserCardLabels();
        for (ActionLabel l: labels) {
            l.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {

                    ActionLabel srcLabel = (ActionLabel) e.getSource();

                    /* Avoid click jitters */
                    if (!frame.isUserCardPaneEnabled())
                        return;

                    /* not exists */
                    if (srcLabel.getIcon() == null)
                        return;

                    frame.setUserCardPaneEnabled(false);
                    srcLabel.setIcon(null);
                    actionToUnveil = srcLabel.action;

                    /* user card choose, notify and unveil */
                    synchronized(waitForTurnState) {
                        waitForTurnState.notifyAll();
                    }
                }
            });
        }

        // Enemy card pane
        // no user-interactive component
    }
    
    private State initState = new State("Initial") {

        @Override
        public void handle() {

            protectCityIndex = -1;
        };

        class InitAction implements MessageAction {

            @Override
            public void actionPerformed(MessageActionEvent event) {

                Object[] payloads = (Object[]) event.getMessage().getPayload();
                myId = (int) payloads[0];
                numOfBosses = (int) payloads[1];
                numOfCities = 2 * numOfBosses - 1;

                actionsUnveiledMap = new HashMap<CITY_ID, List<CardAction>>();
                CITY_ID[] cids = CITY_ID.values();
                for(int i = 0; i < numOfCities; i++) {
                    CITY_ID cid = cids[i];
                    actionsUnveiledMap.put(cid, new ArrayList<CardAction>());
                }

                snapshots = new Snapshot[numOfBosses];
                for(int i = 0; i < numOfBosses; i++)
                    snapshots[i] = new Snapshot(numOfBosses, i);
                snapshots[myId].bossName = username;

                cityWinners = new CityWinner[numOfCities + 1]; // include protector
                for (int i = 0; i < cityWinners.length; i++)
                    cityWinners[i] = new CityWinner(i, snapshots);
                
                frame.init(snapshots, numOfBosses, myId);
                initFrameActions();

                stateChange(dispatchState);
            }
        };

        @Override
        protected void initTransitionActions() {
            actionMap.put(MessageType.INIT, new InitAction());
        };
    };

    /* called when entering dispatch state */
    private void nextHand() {

        actions.removeAll(actions);

        for(List<CardAction> l: actionsUnveiledMap.values())
            l.removeAll(l);

        for (CityWinner w: cityWinners)
            w.nextHand();
    }

    private State dispatchState = new State("Dispatch") {

        @Override
        public void handle() {

            if(protectCityIndex < numOfCities - 1)
                protectCityIndex++;

            nextHand();
            
            frame.nextHandMainPane();
            frame.showCityCards(protectCityIndex);
            frame.setMainPaneEnabled(false);
        };

        class ListAction implements MessageAction {

            @Override
            public void actionPerformed(MessageActionEvent event) {

                Object[] payloads = (Object[]) event.getMessage().getPayload();
                int id = (int) payloads[0];
                String name = (String) payloads[1];
                CITY_ID[] cids = (CITY_ID[]) payloads[2];
                int numOfExpertsInHand = (int) payloads[3];
                int numOfExpertsInHospital = (int) payloads[4];
                int numOfExpertsInPrison = (int) payloads[5];

                snapshots[id].bossName = name;
                snapshots[id].actionCids.addAll(Arrays.asList(cids));
                snapshots[id].nextHand(numOfExpertsInHand,
                                       numOfExpertsInHospital,
                                       numOfExpertsInPrison);

                frame.nextHandEnemyCardPane();
                frame.showEnemyCards(id, cids);
                frame.showUserInfo(id); // it shall be after UI ready
            }
        };

        class DispatchAction implements MessageAction {

            @Override
            public void actionPerformed(MessageActionEvent event) {

                Object[] payloads = (Object[]) event.getMessage().getPayload();                
                CardAction[] dispatch = (CardAction[]) payloads[0];
                int numOfExpertsInHand = (int) payloads[1];
                int numOfExpertsInHospital = (int) payloads[2];
                int numOfExpertsInPrison = (int) payloads[3];
                
                actions.addAll(Arrays.asList(dispatch));

                //CITY_ID[] cids = Boss.getActionCities(dispatch);
                //snapshots[myId].actionCids.addAll(Arrays.asList(cids));
                snapshots[myId].nextHand(numOfExpertsInHand,
                                         numOfExpertsInHospital,
                                         numOfExpertsInPrison);

                frame.nextHandOperationPane();                
                frame.setOperationPaneEnabled(false);                
                frame.showUserCards(dispatch);
                frame.setUserCardPaneEnabled(false);                
                frame.showImprisonAndHospitalize(); // DISPATCH shall be received after LIST

                // get the actions, start the game!
                stateChange(waitForTurnState);
            }			
        };

        class EndAction implements MessageAction {

            @Override
            public void actionPerformed(MessageActionEvent event) {

                // Game over
                stateChange(endState);
            }			
        };

        @Override
        protected void initTransitionActions() {

            actionMap.put(MessageType.LIST, new ListAction());
            actionMap.put(MessageType.DISPATCH, new DispatchAction());
            actionMap.put(MessageType.END, new EndAction());
        };
    };

    private CardAction actionToUnveil;
    private int cidToPutGangsters, numOfExpertsToPut, numOfOccasionalsToPut;

    private State waitForTurnState = new State("WaitForTurn") {

        @Override
        public void handle() { /* do nothing */ };

        class TurnAction implements MessageAction {

            @Override
            public void actionPerformed(MessageActionEvent event) {

                int id = (int) event.getMessage().getPayload();

                if(id != myId) {
                    System.out.println("<< It's id-" + id + "'s turn");
                    frame.showUserInfo(id, "<<<");
                    return;
                } else {
                    System.out.println(">> It's My turn");
                    frame.showUserInfo(id, ">>> your turn");
                }

                if(actions.size() == 0)
                    throw new RuntimeException("No card to unveil");

                try {

                    // Let user unveil a card
                    frame.setUserCardPaneEnabled(true);

                    synchronized(waitForTurnState) {
                        waitForTurnState.wait();
                    }

                    updateUnveil();

                    /* Let user occupy a city 
                     * execBtn is enabled after city is chose
                     * passBtn is always enabled
                     */
                    frame.setOperationPaneEnabled(
                            snapshots[myId].expertsInHand,
                            snapshots[myId].occasionalsInHand,
                            false);
                    
                    /* enabled will be set to false after execBtn/passBtn is pressed */
                    if (snapshots[myId].expertsInHand != 0 ||
                        snapshots[myId].occasionalsInHand != 0)
                        frame.setMainPaneEnabled(true);

                    synchronized(waitForTurnState) {
                        waitForTurnState.wait();
                    }

                    updateOccupy();

                } catch (IOException | InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }	
        };

        private void updateUnveil() throws IOException {

            if(actionToUnveil == null)
                throw new RuntimeException("No card to unveil");

            actionMap.get(MessageType.UPDATE_UNVEIL)
            .actionPerformed(
                    new MessageActionEvent(
                            null,
                            new Message(
                                    MessageType.UPDATE_UNVEIL,
                                    new Object[]{myId, actionToUnveil})
                            ));
            mioToBoard.writeMessage(MessageType.UNVEIL,
                    new Object[]{myId, actionToUnveil});
            actionToUnveil = null;
        }

        private void updateOccupy() throws IOException {

            if (cidToPutGangsters == -1) {

                actionMap.get(MessageType.UPDATE_PASS)
                .actionPerformed(
                        new MessageActionEvent(
                                null,
                                new Message(
                                        MessageType.UPDATE_PASS,
                                        new Object[]{myId})
                                ));
                mioToBoard.writeMessage(MessageType.PASS,
                        new Object[]{myId});
                return;
            }

            int experts = numOfExpertsToPut;
            int occasionals = numOfOccasionalsToPut;

            if (cidToPutGangsters == protectCityIndex + 1) {
                actionMap.get(MessageType.UPDATE_PROTECT)
                .actionPerformed(
                        new MessageActionEvent(
                                null,
                                new Message(
                                        MessageType.UPDATE_PROTECT,
                                        new Object[]{myId, experts, occasionals})
                                ));
                mioToBoard.writeMessage(MessageType.PROTECT,
                        new Object[]{myId, experts, occasionals});
            } else {
                /* translate to the cid that server recognizes (protector excluded) */
                int cid = cidToPutGangsters > protectCityIndex ?
                          cidToPutGangsters - 1: cidToPutGangsters;
                actionMap.get(MessageType.UPDATE_OCCUPY)
                .actionPerformed(
                        new MessageActionEvent(
                                null,
                                new Message(
                                        MessageType.UPDATE_OCCUPY,
                                        new Object[]{myId, cid, experts, occasionals})
                                ));
                mioToBoard.writeMessage(MessageType.OCCUPY,
                        new Object[]{myId, cid, experts, occasionals});
            }
        }

        class UpdateUnveilAction implements MessageAction {

            @Override
            public void actionPerformed(MessageActionEvent event) {

                Object[] payloads = (Object[]) event.getMessage().getPayload();
                int id = (int) payloads[0];
                CardAction actionToUnveil = (CardAction) payloads[1];
                CITY_ID cid = actionToUnveil.getCid();

                actionsUnveiledMap.get(cid).add(actionToUnveil);
                snapshots[id].actionCids.remove(cid);				
                updateSnapshot();

                frame.unveilActionCard(actionToUnveil);

                if (id != myId)
                    frame.removeEnemyCards(id, cid);
            }			
        };

        class UpdateOccupyAction implements MessageAction {

            @Override
            public void actionPerformed(MessageActionEvent event) {

                Object[] payloads = (Object[]) event.getMessage().getPayload();
                int id = (int) payloads[0];
                int cid = (int) payloads[1];
                int numOfExperts = (int) payloads[2];
                int numOfOccasionals = (int) payloads[3];

                if (cid > protectCityIndex)
                    cid++;

                cityWinners[cid].setCityWinner(id, numOfExperts + numOfOccasionals);
                snapshots[id].occupyCity(cid, numOfExperts, numOfOccasionals);
                updateSnapshot();

                frame.updateGangsters(cid);
                frame.showUserInfo(id);
            }			
        };

        class UpdateProtectAction implements MessageAction {

            @Override
            public void actionPerformed(MessageActionEvent event) {

                Object[] payloads = (Object[]) event.getMessage().getPayload();
                int id = (int) payloads[0];
                int numOfExperts = (int) payloads[1];
                int numOfOccasionals = (int) payloads[2];

                int cid = protectCityIndex + 1;
                cityWinners[cid].setCityWinner(id, numOfExperts + numOfOccasionals);
                snapshots[id].occupyCity(cid, numOfExperts, numOfOccasionals);
                updateSnapshot();

                frame.updateGangsters(cid);
                frame.showUserInfo(id);
            }			
        };

        class UpdatePassAction implements MessageAction {

            @Override
            public void actionPerformed(MessageActionEvent event) {

                Object[] payloads = (Object[]) event.getMessage().getPayload();
                int id = (int) payloads[0];

                frame.showUserInfo(id);
            }           
        };

        class FinishHandAction implements MessageAction {

            @Override
            public void actionPerformed(MessageActionEvent event) {

                stateChange(scoreState);
            }			
        };

        @Override
        protected void initTransitionActions() {

            actionMap.put(MessageType.TURN, new TurnAction());
            actionMap.put(MessageType.UPDATE_UNVEIL, new UpdateUnveilAction());
            actionMap.put(MessageType.UPDATE_OCCUPY, new UpdateOccupyAction());
            actionMap.put(MessageType.UPDATE_PROTECT, new UpdateProtectAction());
            actionMap.put(MessageType.UPDATE_PASS, new UpdatePassAction());
            actionMap.put(MessageType.FINISH_HAND, new FinishHandAction());
        };
    };

    private State scoreState = new State("Score") {

        @Override
        public void handle() { /* no actions */	};

        class ScoreAction implements MessageAction {

            @Override
            public void actionPerformed(MessageActionEvent event) {

                Object[] payloads = (Object[]) event.getMessage().getPayload();
                int id = (int) payloads[0];
                int reward = (int) payloads[1];

                snapshots[id].reward(reward);

                // Server broadcasts to all users
                if (id == myId)
                    JOptionPane.showMessageDialog(frame, "You got reward$" + reward, "Next hand!!", JOptionPane.INFORMATION_MESSAGE);
                frame.showUserInfo(id, "(+" + reward + ")");

                numOfBossesScored++;
                if(numOfBossesScored == numOfBosses) {

                    updateSnapshot();

                    // Wait for next hand
                    numOfBossesScored = 0;
                    stateChange(dispatchState);
                }
            }
        };


        class LastUnveilAction implements MessageAction {

            @Override
            public void actionPerformed(MessageActionEvent event) {

                CardAction[] lastActions = (CardAction[]) event.getMessage().getPayload();

                frame.showLastUnveilDialog(lastActions, cityWinners);
                System.out.println("Show the last card of each city...");
                for(CardAction action: lastActions)
                    System.out.println(action);
            }
        };

        @Override
        protected void initTransitionActions() {

            actionMap.put(MessageType.SCORE, new ScoreAction());
            actionMap.put(MessageType.LAST_UNVEIL, new LastUnveilAction());
        };
    };

    private State endState = new State("End") {

        @Override
        public void handle() {

            for(Snapshot s: snapshots)
                s.actionCids = null;

            System.exit(0);
        };

        @Override
        protected void initTransitionActions() { /* no actions */ };
    };

    private void updateSnapshot() {

        for(int id = 0; id < numOfBosses; id++) {
            System.out.println("= id-" + id + " =");

            if(id == myId) {
                System.out.println("\t>>> me");
                for(CardAction a: actions)
                    System.out.println("\t" + a);
            } else {
                for(CITY_ID cid: snapshots[id].actionCids)
                    System.out.println("\t[" + cid + "]");
            }

            System.out.println(snapshots[id]);
        }

        System.out.println("** CITIES' STATUS **");

        CITY_ID[] cids = CITY_ID.values();
        for(int i = 0; i< numOfCities; i++) {
            CITY_ID cid = cids[i];
            System.out.println("\t" + cid + ":");

            for(CardAction a: actionsUnveiledMap.get(cid)) {
                System.out.println("\t" + a);
            }

            if (cid.ordinal() > protectCityIndex) /* protector is one of elements in snapshots */
                updateOccupacy(cid.ordinal() + 1);
            else
                updateOccupacy(cid.ordinal());

            if(cid.ordinal() == protectCityIndex) {
                System.out.println("\t(Protector) " + CITY_ID.CHICAGO);
                updateOccupacy(protectCityIndex + 1);
            }
        }

        System.out.println();
    }

    private void updateOccupacy(int cid) {

        for(int id = 0; id < numOfBosses; id++) {
            int experts = snapshots[id].expertsInCity[cid];
            int occasionals = snapshots[id].occasionalsInCity[cid];

            if(experts != 0 || occasionals != 0) {
                System.out.print("\t\tid-" + id + " occupies ");
                if(experts != 0 && occasionals != 0)
                    System.out.println(experts + " Experts and " + occasionals + " Occasionals.");
                else if(experts != 0)
                    System.out.println(experts + " Experts.");
                else
                    System.out.println(occasionals + " Occasionals.");					
            }
        }		
    }
}
