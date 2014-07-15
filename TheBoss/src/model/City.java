package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.constant.ACTION_TYPE;
import model.constant.CITY_ID;
import model.impl.Boss;
import model.impl.Gangster;
import model.impl.action.Reward;


public abstract class City implements Serializable {

	private static final long serialVersionUID = 1L;
	
	protected CITY_ID id;
	protected Map<Boss, List<Gangster>> gangsterMap;
	protected List<CardAction> actions, unShowenActions;
	
	public City(CITY_ID id) {
		
		this.id = id;
		gangsterMap = new HashMap<Boss, List<Gangster>>(
							TheBossBoardGame.MAX_NUM_OF_BOSSES);
		actions = new ArrayList<CardAction>();
		unShowenActions = new ArrayList<CardAction>();
		
		createActions();
		nextHand();
	}
	
	protected abstract void createActions();
	
	public CardAction[] getActions(){
		
		return actions.toArray(new CardAction[actions.size()]);
	}
	
	public void nextHand() {
		
		unShowenActions.clear();
		unShowenActions.addAll(actions);
		
		for(List<Gangster> g: gangsterMap.values())
			g.clear();
		gangsterMap.clear();
	}
	
	public void removeProtector() {
	
        for(CardAction a: actions){
            if(a.getType() == ACTION_TYPE.REWARD)
                ((Reward) a).removeRewardListener();
        }
	}
	
	public void addProtector(RewardListener protector) {
	
		// need to give some fee to the protector...
		for(CardAction a: actions){
			if(a.getType() == ACTION_TYPE.REWARD)
				((Reward) a).addRewardListener(protector);
		}
	}
	
	public void occupy(Boss boss, Gangster gangster) {
		
		if(!gangsterMap.containsKey(boss)){
			gangsterMap.put(boss, new ArrayList<Gangster>());
		}
		gangsterMap.get(boss).add(gangster);
	}
	
	public void showAction(CardAction actionShown) {
		
		unShowenActions.remove(actionShown);
	}
	
	public CardAction winningOrSanction() {
		
		if(unShowenActions.size() != 1)
			throw new RuntimeException("unShwonActions.size shall be 1");
		
		CardAction lastAction = unShowenActions.get(0);
		lastAction.execute(gangsterMap);
		
		return lastAction;
	}
	
	@Override
	public String toString() {
		
		return id.toString();
	}
	
	// for debug
	public void printGangsterMap() {

        System.out.println("====" + this + "====");
	    for (Boss b: gangsterMap.keySet()) {
	        List<Gangster> g = gangsterMap.get(b);
	        System.out.println(b.getId() + "(" + b.getName() + ") occupies " + g.size() + " gangsters.");
	    }
	}
}
