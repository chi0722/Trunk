package model.impl.city;

import model.City;
import model.constant.CITY_ID;
import model.impl.action.Shoot;
import model.impl.action.Reward;

public class Detroit extends City {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/* Detroit has 5 credits:
	 * 	$, $$, $$$, $$$$, gun
	 */
	public Detroit() {
		
		super(CITY_ID.DETROIT);
	}

	@Override
	protected void createActions() {

		for(int i = 1; i <= 4; i++){
			actions.add(new Reward(i, id));
		}
		actions.add(new Shoot(id));
	}

}
