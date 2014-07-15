package model.impl.city;

import model.City;
import model.constant.CITY_ID;
import model.impl.action.Banish;
import model.impl.action.Reward;

public class Cincinnati extends City {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/* Cincinnati has 4 credits:
	 * 	$, $$, $$$, ban
	 */
	public Cincinnati() {
		
		super(CITY_ID.CINCINNATI);
	}

	@Override
	protected void createActions() {

		for(int i = 1; i <= 3; i++){
			actions.add(new Reward(i, id));
		}
		actions.add(new Banish(id));
	}

}
