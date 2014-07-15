package model.impl.city;

import model.CardAction;
import model.City;
import model.RewardListener;
import model.constant.CITY_ID;
import model.impl.action.Reward;

public class Chicago extends City implements RewardListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/* Chicago has no default credit.
	 * it always shares the reward from other cities. 
	 */
	public Chicago() {
		
		super(CITY_ID.CHICAGO);
	}

	@Override
	protected void createActions() {
		// do nothing
	}
	
	@Override
	public void showAction(CardAction creditShown) {
		// do nothing
	}

	@Override
	public void reward(int money) {
		
		CardAction haldReward = new Reward(money/2, id);
		haldReward.execute(gangsterMap);
	}
}
