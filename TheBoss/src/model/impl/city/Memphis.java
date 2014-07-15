package model.impl.city;

import model.City;
import model.constant.CITY_ID;
import model.impl.action.Hospitalize;
import model.impl.action.Reward;

public class Memphis extends City {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/* Memphis has 4 credits:
	 * 	$, $$, $$$, hospital
	 */
	public Memphis() {
		
		super(CITY_ID.MEMPHIS);
	}

	@Override
	protected void createActions() {

		for(int i = 1; i <= 3; i++){
			actions.add(new Reward(i, id));
		}		
		actions.add(new Hospitalize(id));
	}

}
