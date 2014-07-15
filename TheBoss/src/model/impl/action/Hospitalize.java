package model.impl.action;

import java.util.List;
import java.util.Map;

import model.CardAction;
import model.constant.ACTION_TYPE;
import model.constant.CITY_ID;
import model.impl.Boss;
import model.impl.Gangster;

public class Hospitalize extends CardAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Hospitalize(CITY_ID cid){
	
		super(ACTION_TYPE.HOSPITALIZE, cid);
	}
	
	@Override
	public void execute(Map<Boss, List<Gangster>> gangsterMap) {

		for(Boss boss: gangsterMap.keySet()){
			List<Gangster> gangsters = gangsterMap.get(boss);
			filterOccasionals(gangsters);
			boss.hospitalize(gangsters);
		}
	}
}
