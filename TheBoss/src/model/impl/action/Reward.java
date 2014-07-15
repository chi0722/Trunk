package model.impl.action;

import java.util.List;
import java.util.Map;

import model.CardAction;
import model.RewardListener;
import model.constant.ACTION_TYPE;
import model.constant.CITY_ID;
import model.impl.Boss;
import model.impl.Gangster;

public class Reward extends CardAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int MAX_VALUE = 10;
	private int money;
	private RewardListener listener;
	private int hashcode;
	
	
	public Reward(int value, CITY_ID cid) {
	
		super(ACTION_TYPE.REWARD, cid);
		
		if(value > MAX_VALUE)
			throw new RuntimeException("Money exceeds the maximum");
		
		money = value;
		hashcode = super.hashCode() * MAX_VALUE + value;
	}
	
	public int getValue() {
		
		return money;
	}
	
	public void addRewardListener(RewardListener listener) {
		
		this.listener = listener;
	}
	
	public void removeRewardListener() {
	    
	    this.listener = null;
	}
	
	@Override
	public void execute(Map<Boss, List<Gangster>> gangsterMap) {

		// always & only notify the listener once!
		if(listener != null){
			listener.reward(money);
		}
		
		if(gangsterMap.size() == 0)
			return;
		
		Object[] bosses = gangsterMap.keySet().toArray();
		Boss bossWhoWins = (Boss)bosses[0];
		int maxNumOfGangsters = gangsterMap.get(bossWhoWins).size();
		
		for(int i = 1; i < bosses.length; i++){
			Boss b = (Boss) bosses[i];
			int numOfGangsters = gangsterMap.get(b).size();
			if(numOfGangsters > maxNumOfGangsters){
				maxNumOfGangsters = numOfGangsters;
				bossWhoWins = b;
			}else if(numOfGangsters == maxNumOfGangsters){
				// cannot share the reward!!
				bossWhoWins = null;
			}
		}
		
		if(bossWhoWins != null)
			bossWhoWins.reward(money);
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if(obj instanceof Reward) {
			Reward action = (Reward) obj;
			return (getCid() == action.getCid() && getValue() == action.getValue());
		} else
			return false;
	}
	
	@Override
	public int hashCode() {
		
		return hashcode;
	}
	
	@Override
	public String toString(){
		
		return super.toString() + "(" + money + ")";
	}
}
