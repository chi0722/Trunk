package model.impl.city;

import model.City;
import model.constant.CITY_ID;
import model.impl.action.Hospitalize;
import model.impl.action.Imprison;
import model.impl.action.Reward;

public class Boston extends City {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/* Boston has 4 credits:
	 * 	$$$, $$$, jail, hospital
	 */
	public Boston() {
		
		super(CITY_ID.BOSTON);
	}

	@Override
	protected void createActions() {

		for(int i = 0; i < 2; i++){
			actions.add(new Reward(3, id));
		}
		actions.add(new Imprison(id));
		actions.add(new Hospitalize(id));
	}
}
