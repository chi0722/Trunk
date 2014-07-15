package model.impl.city;

import model.City;
import model.constant.CITY_ID;
import model.impl.action.Shoot;
import model.impl.action.Reward;

public class KansasCity extends City {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/* Kansas City has 3 credits:
	 * 	$$, $$$, gun
	 */
	public KansasCity() {
		
		super(CITY_ID.KANSAS_CITY);
	}

	@Override
	protected void createActions() {

		for(int i = 2; i <= 3; i++){
			actions.add(new Reward(i, id));
		}
		actions.add(new Shoot(id));
	}

}
