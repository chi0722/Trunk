package model.impl.city;

import model.City;
import model.constant.CITY_ID;
import model.impl.action.Reward;

public class NewYork extends City {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/* New York has 4 credits:
	 * 	0, $, $$, $$$
	 */
	public NewYork() {
		
		super(CITY_ID.NEW_YORK);
	}

	@Override
	protected void createActions() {

		for(int i = 0; i <= 3; i++){
			actions.add(new Reward(i, id));
		}
	}

}
