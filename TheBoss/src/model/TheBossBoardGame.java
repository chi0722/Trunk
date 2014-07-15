package model;

import io.MessageIO;
import io.MessageIOHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import model.impl.Boss;
import model.impl.city.Boston;
import model.impl.city.Chicago;
import model.impl.city.Cincinnati;
import model.impl.city.Detroit;
import model.impl.city.KansasCity;
import model.impl.city.Memphis;
import model.impl.city.NewYork;
import model.impl.city.Philadelphia;
import util.MessageAction;
import util.MessageActionEvent;
import util.MessageType;
import control.State;
import control.Unit;

public class TheBossBoardGame extends Unit {

    public final static int MAX_NUM_OF_BOSSES = 4;
    public final static int MAX_HANDS_IN_A_GAME = 5;
    public final static int NUM_TURNS_IN_A_HAND_PER_BOSS = 5;
    public final static int PORT = 1024;
    public final static RewardListener PROTECTOR = new Chicago();

    /* shall be the same indexes as CITY_ID */
    public final static City[] CITIES = { new NewYork(),
                                          new Boston(),
                                          new Detroit(),
                                          new KansasCity(),
                                          new Cincinnati(),
                                          new Memphis(),
                                          new Philadelphia() };
    private int numOfCities;
    private int numOfBosses;
    private int numOfGoldenPolice, numOfSilverPolice;
    private int protectCityIndex;
    private int turnOffset, currentTurnIndex, numOfRoundsInAHand;
    private int totalId;
    private Boss[] bosses, bossesInTurns;
    private Map<Integer, MessageIO> bossIOMap; // id to mio

    public TheBossBoardGame(int numOfUsers) {

        numOfBosses = numOfUsers;
        numOfCities = 2 * numOfUsers - 1;
        bosses = new Boss[numOfUsers];
        bossesInTurns = new Boss[numOfUsers];
        bossIOMap = new HashMap<Integer, MessageIO>(numOfUsers);

        ServerSocket serverSocket = null;

        while(true){
            
            stateChange(initialState);
            
            try {
                serverSocket = new ServerSocket(PORT);

                Thread[] handlers = new Thread[numOfUsers];
                for (int i = 0; i < numOfUsers; i++){
                    MessageIO mio = new MessageIO(serverSocket.accept());

                    // start a thread to monitor incoming messages & notify
                    MessageIOHandler handler = new MessageIOHandler(mio);
                    handler.setMessageListener(this);
                    handler.start();
                    handlers[i] = handler;
                }

                for (int i = 0; i < numOfUsers; i++)
                    handlers[i].join();

            } catch (IOException e) {
                e.printStackTrace();
                break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (serverSocket != null) {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        System.err.println("Server close error - " + e);
                    }
                }
            }
        }		
    }

    private boolean isIOEHandled = false;
    @Override
    public void handleIOException(MessageIO src, IOException e) {
        
        if (isIOEHandled)
            return;
        
        isIOEHandled = true;
        
        try {
            this.broadcastExclusive(
                    MessageType.EXCEPTION,
                    null,
                    src);
            for (MessageIO mio: bossIOMap.values()) {
                if (mio == src)
                    continue;
                mio.close();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    public boolean isTerminated() {

        int maxRunEach = (MAX_HANDS_IN_A_GAME + 1) / 2;
        if(new Random().nextInt(2) == 0)
            numOfGoldenPolice++;
        else
            numOfSilverPolice++;

        return (numOfGoldenPolice == maxRunEach || 
                numOfSilverPolice == maxRunEach);
    }

    public void nextHand(Boss[] bosses) {


        // Choose one action card to put next to each city
        List<CardAction> allActions = new ArrayList<CardAction>();
        Random rand = new Random();
        for(int i = 0; i< numOfCities; i++) {
            CardAction[] actions = CITIES[i].getActions();
            int len = actions.length;
            int keepIndex = rand.nextInt(len);

            CITIES[i].nextHand();

            // don't copy keepIndex to allActions, so we shrink it
            System.out.println(actions[keepIndex]);
            actions[keepIndex] = actions[len-1];
            actions = Arrays.copyOf(actions, len-1);
            allActions.addAll(Arrays.asList(actions));
        }

        // Change Chicago to protect the next city after each hand
        ((City)PROTECTOR).nextHand();
        
        if (protectCityIndex != -1)
            CITIES[protectCityIndex].removeProtector(); // remove previous setting

        // Don't change if it protects the city in the most right side
        if(protectCityIndex < numOfCities - 1)
            protectCityIndex++;
        
        CITIES[protectCityIndex].addProtector(PROTECTOR);
        
        
        // Randomizing left cards before dispatching to users
        Collections.shuffle(allActions);

        int fromIdx = 0;
        int NumOfActionsEach = allActions.size() / bosses.length;
        assert(NumOfActionsEach == NUM_TURNS_IN_A_HAND_PER_BOSS);

        for(Boss b: bosses) {
            b.nextHand(
                    allActions.subList(fromIdx, fromIdx + NumOfActionsEach));
            fromIdx += NumOfActionsEach;
            System.out.println(b);
        }
    }

    private void broadcast(
            MessageType type, Object payload)
                    throws IOException {

        for(MessageIO mio: bossIOMap.values()) {
            mio.writeMessage(type, payload);
        }	
    }

    private void broadcastExclusive(
            MessageType type, Object payload, MessageIO mioExcluded)
                    throws IOException {

        for(MessageIO mio: bossIOMap.values()) {
            if(mio == mioExcluded)
                continue;
            mio.writeMessage(type, payload);
        }
    }

    /*
     * State Machine
     */
     private State initialState = new State("Initial") {

         @Override
         public void handle() {
             
             isIOEHandled = false;
             bossIOMap.clear();
             Arrays.fill(bosses, null);
             
             totalId = 0;
             protectCityIndex = -1;
             currentTurnIndex = 0;
             turnOffset = numOfBosses - 1; // It will be updated to 0 when dispatching
         }

         class RegisterAction implements MessageAction {

             @Override
             public void actionPerformed(MessageActionEvent event) {

                 // User will register his/her name
                 MessageIO srcMio = (MessageIO) event.getSource();
                 String name = (String) event.getMessage().getPayload();

                 try {
                     int myId = totalId++;		
                     bosses[myId] = new Boss(name, myId);
                     bossIOMap.put(myId, srcMio);
                     srcMio.writeMessage(MessageType.INIT, new Object[]{myId, numOfBosses});

                     if(totalId == numOfBosses) {
                         // All bosses have registered. Let's start the game!
                         stateChange(dispatchState);
                     }				
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }		
         };

         @Override
         protected void initTransitionActions() {

             actionMap.put(MessageType.REGISTER, new RegisterAction());
         }
     };

     private State dispatchState = new State("Dispatch") {

         @Override
         public void handle() {

             // first one change to the next
             turnOffset = (turnOffset + 1) % numOfBosses;
             numOfRoundsInAHand = 0;

             // Dispatch cards
             nextHand(bosses);
             
             for(int id: bossIOMap.keySet()) {
                 MessageIO mio = bossIOMap.get(id);
                 try {
                     // Only show city of cards to other users
                     broadcastExclusive(
                             MessageType.LIST, 
                             new Object[]{ 
                                 id,
                                 bosses[id].getName(),
                                 Boss.getActionCities(bosses[id].getActions()),
                                 bosses[id].getNumOfLeftExperts(),
                                 bosses[id].getNumOfExpertsInHospital(),
                                 bosses[id].getNumOfExpertsInPrison()
                             },
                             mio);
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }

             // Ensure that DISPATCH is received after LIST
             for(int id: bossIOMap.keySet()) {
                 MessageIO mio = bossIOMap.get(id);
                 try {					
                     // Dispatch cards for this user
                     mio.writeMessage(
                             MessageType.DISPATCH,
                             new Object[] {
                                 bosses[id].getActions(),
                                 bosses[id].getNumOfLeftExperts(),
                                 bosses[id].getNumOfExpertsInHospital(),
                                 bosses[id].getNumOfExpertsInPrison()
                             });
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }

             // Start turns!
             System.arraycopy(bosses, 0, bossesInTurns, 0, numOfBosses);			
             stateChange(turnState);
         }

         @Override
         protected void initTransitionActions() { /* no actions */ };
     };

     private State turnState = new State("Turn") {

         @Override
         public void handle() {

             if(numOfRoundsInAHand == NUM_TURNS_IN_A_HAND_PER_BOSS) {
                 try {
                     broadcast(MessageType.FINISH_HAND, null);
                 } catch(IOException e) {
                     e.printStackTrace();
                 }
                 // Finish one hand
                 stateChange(scoreState);
                 return;
             }

             if( (numOfRoundsInAHand == NUM_TURNS_IN_A_HAND_PER_BOSS - 1) &&
                     (currentTurnIndex == 0) ) {
                 // Final Turn! The boss with the most Gangsters starts first
                 Arrays.sort(bossesInTurns, new Comparator<Boss>(){
                     @Override
                     public int compare(Boss o1, Boss o2) {
                         int g1 = o1.getNumOfLeftExperts() + o1.getNumOfLeftOccasionals();
                         int g2 = o2.getNumOfLeftExperts() + o2.getNumOfLeftOccasionals();
                         return g2 - g1;
                     }				
                 });
             }

             Boss bossThisTurn = bossesInTurns[(turnOffset + currentTurnIndex) % numOfBosses];
             currentTurnIndex++;
             if(currentTurnIndex == numOfBosses) {
                 currentTurnIndex = 0;
                 numOfRoundsInAHand++;
             }

             try {
                 broadcast(MessageType.TURN, bossThisTurn.getId());
             } catch(IOException e) {
                 e.printStackTrace();
             }			
             // wait for user showing one of his/her card
         }

         class UnveilAction implements MessageAction {

             @Override
             public void actionPerformed(MessageActionEvent event) {

                 Object[] payloads = (Object[]) event.getMessage().getPayload();
                 int id = (int) payloads[0];
                 CardAction actionToUnveil = (CardAction) payloads[1];

                 System.out.println("id-" + id + " unveils " + actionToUnveil);
                 bosses[id].unveilAction(actionToUnveil);

                 try {
                     MessageIO mio = (MessageIO) event.getSource();
                     broadcastExclusive(
                             MessageType.UPDATE_UNVEIL,
                             new Object[]{id, actionToUnveil},
                             mio);
                 } catch(IOException e) {
                     e.printStackTrace();
                 }

                 stateChange(occupyState);
             }
         };

         @Override
         protected void initTransitionActions() {

             actionMap.put(MessageType.UNVEIL, new UnveilAction());
         }
     };

     private State occupyState = new State("Occupy") {

         @Override
         public void handle() { /* do nothing */ };

         class OccupyAction implements MessageAction {

             @Override
             public void actionPerformed(MessageActionEvent event) {

                 Object[] payloads = (Object[]) event.getMessage().getPayload();
                 int id = (int) payloads[0];
                 int cid = (int) payloads[1];
                 int NumOfExperts = (int) payloads[2];
                 int NumOfOccasionals = (int) payloads[3];

                 System.out.println("id-" + id + " occupies " + CITIES[cid] + 
                         "(" + NumOfExperts + "," + NumOfOccasionals + ")");
                 bosses[id].occupy(CITIES[cid], NumOfExperts, NumOfOccasionals);

                 try {
                     broadcastExclusive(
                             MessageType.UPDATE_OCCUPY,
                             payloads,
                             (MessageIO) event.getSource());
                 } catch(IOException e) {
                     e.printStackTrace();
                 }

                 stateChange(turnState);
             }
         };

         class ProtectAction implements MessageAction {

             @Override
             public void actionPerformed(MessageActionEvent event) {

                 Object[] payloads = (Object[]) event.getMessage().getPayload();
                 int id = (int) payloads[0];
                 int NumOfExperts = (int) payloads[1];
                 int NumOfOccasionals = (int) payloads[2];

                 System.out.println("id-" + id + " occupies protector" + 
                         "(" + NumOfExperts + "," + NumOfOccasionals + ")");
                 bosses[id].occupy((City)PROTECTOR, NumOfExperts, NumOfOccasionals);

                 try {
                     broadcastExclusive(
                             MessageType.UPDATE_PROTECT,
                             payloads,
                             (MessageIO) event.getSource());
                 } catch(IOException e) {
                     e.printStackTrace();
                 }

                 stateChange(turnState);
             }
         };

         class PassAction implements MessageAction {

             @Override
             public void actionPerformed(MessageActionEvent event) {

                 Object[] payloads = (Object[]) event.getMessage().getPayload();

                 try {
                     broadcastExclusive(
                             MessageType.UPDATE_PASS,
                             payloads,
                             (MessageIO) event.getSource());
                 } catch(IOException e) {
                     e.printStackTrace();
                 }

                 stateChange(turnState);
             }
         };

         @Override
         protected void initTransitionActions() {

             actionMap.put(MessageType.OCCUPY, new OccupyAction());
             actionMap.put(MessageType.PROTECT, new ProtectAction());
             actionMap.put(MessageType.PASS, new PassAction());
         }
     };

     private State scoreState = new State("Score") {

         @Override
         public void handle() {

             // Show rewards and sanctions
             try {
                 CardAction[] actions = new CardAction[numOfCities];
                 for(int i = 0; i < numOfCities; i++) {
                     CITIES[i].printGangsterMap();
                     actions[i] = CITIES[i].winningOrSanction();
                     System.out.println(actions[i]);
                 }

                 broadcast(MessageType.LAST_UNVEIL, actions);
                 for(Boss boss: bosses) {
                     System.out.println(boss);
                     broadcast(
                             MessageType.SCORE,
                             new Object[]{
                                 boss.getId(),
                                 boss.getReward()
                             });
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             }

             if(isTerminated())
                 stateChange(endState);
             else
                 stateChange(dispatchState);
         }

         @Override
         protected void initTransitionActions() { /* no actions */ };
     };

     private State endState = new State("End") {

         @Override
         public void handle() {

             // TODO: ask if users what to play again & change to initial state
             try {
                 broadcast(MessageType.END, null);
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }

         @Override
         protected void initTransitionActions() { /* no actions */ };
     };

     public static void main(String[] args){

         /* args[0]: boolean isServer
          * args[1]: int numOfBosses
          * args[2]: port
          */
         new TheBossBoardGame(Integer.parseInt(args[0]));
     }
}
