package model.impl.city;

import model.City;
import model.constant.CITY_ID;
import model.impl.action.Shoot;
import model.impl.action.Imprison;
import model.impl.action.Reward;

public class Philadelphia extends City {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/* Philadelphia has 3 credits:
	 * 	$$$, gun, jail
	 */
	public Philadelphia() {
		
		super(CITY_ID.PHILADELPHIA);
	}

	@Override
	protected void createActions() {

		actions.add(new Reward(3, id));
		actions.add(new Shoot(id));
		actions.add(new Imprison(id));
	}

}
