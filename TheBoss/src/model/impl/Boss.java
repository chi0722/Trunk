package model.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import model.CardAction;
import model.City;
import model.TheBossBoardGame;
import model.constant.CITY_ID;
import model.constant.GANGSTER_TYPE;

public class Boss {

	public static final int MAX_NUM_OF_EXPERTS = 6;
	public static final int MAX_NUM_OF_OCCASONALS = 3;
	private String name;
	private int id;
	private int money;
	private List<CardAction> actionsInHand;
	private List<Gangster> expertsInHand,
						   expertsInCity,
						   occasionalsInHand,
						   expertsInPrison,
						   expertsInHospital;
	
	public Boss(String name, int id) {
		
		this.name = name;
		this.id = id;
		this.money = 0;
		
		actionsInHand = new ArrayList<CardAction>();
		
		expertsInHand = new ArrayList<Gangster>();
		expertsInCity = new ArrayList<Gangster>();
		for(int i = 0; i < MAX_NUM_OF_EXPERTS; i++)
			expertsInHand.add(new Gangster(GANGSTER_TYPE.EXPERT));
		
		occasionalsInHand = new ArrayList<Gangster>();
		for(int i = 0; i < MAX_NUM_OF_OCCASONALS; i++)
			occasionalsInHand.add(new Gangster(GANGSTER_TYPE.OCCASIONAL));
		
		expertsInPrison = new ArrayList<Gangster>();
		expertsInHospital = new ArrayList<Gangster>();
	}
	
	public int getId() { return id; }
	public String getName() { return name; }
	public int getNumOfLeftExperts() { return expertsInHand.size(); }	
	public int getNumOfLeftOccasionals() { return occasionalsInHand.size(); }
	//public int getNumOfExpertsInCity() { return expertsInCity.size(); }
	public int getNumOfExpertsInPrison() { return expertsInPrison.size(); }
	public int getNumOfExpertsInHospital() { return expertsInHospital.size(); }
	
	public CardAction[] getActions() {
		
		return actionsInHand.toArray(new CardAction[actionsInHand.size()]);
	}
	
	public static CITY_ID[] getActionCities(CardAction[] actions) {
		
		CITY_ID[] ids = new CITY_ID[actions.length];
		for(int i = 0; i < actions.length; i++) {
			ids[i] = actions[i].getCid();
		}
		Arrays.sort(ids);
		
		return ids;
	}
	
	public void nextHand(List<CardAction> actions) {
	
		// put back experts used in the previous hand
		Iterator<Gangster> i = expertsInPrison.iterator();
		while(i.hasNext()) {
			Gangster g = i.next();
			if(g.inSanction())
				g.impose(); // don't put back but impose once
			else {
				expertsInHand.add(g);
				i.remove();
			}
		}
		
		i = expertsInHospital.iterator();
		while(i.hasNext()) {
			Gangster g = i.next();
			if(g.inSanction())
				g.impose(); // don't put back but impose once
			else {
				expertsInHand.add(g);
				i.remove();
			}
		}

		expertsInHand.addAll(expertsInCity);
		expertsInCity.clear();
		
		// dispatch cards
		actionsInHand.clear();
		actionsInHand.addAll(actions);
	}
	
	public void unveilAction(CardAction actionToUnveil) {
		
		actionsInHand.remove(actionToUnveil);
		City belongCity = TheBossBoardGame.CITIES[actionToUnveil.getCid().ordinal()];
		belongCity.showAction(actionToUnveil);
	}
	
	public void occupy(City city, int NumOfExperts, int NumOfOccasional) {
		
		for(int i=0; i< NumOfExperts; i++){
			Gangster e = expertsInHand.remove(0);
			expertsInCity.add(e);
			city.occupy(this, e);
		}

		for(int i=0; i< NumOfOccasional; i++){
			// Occasional cannot be re-used
			Gangster o = occasionalsInHand.remove(0);
			city.occupy(this, o);
		}
	}

	public void reward(int money) {
		
		this.money += money;
	}
	
	public int getReward() {
		
		return money;
	}
	
	public void shoot(List<Gangster> experts) {
		
		// Experts are killed by gun shots!
		for(Gangster g: experts)
			expertsInCity.remove(g);
	}
	
	public void imprison(List<Gangster> experts) {
		
		// move Experts from the city to the prison
		for(Gangster g: experts) {
			expertsInCity.remove(g);
			expertsInPrison.add(g);
			g.imprison();
		}
	}
	
	public void hospitalize(List<Gangster> experts) {
		
		// move Experts from the city to the hospital
		for(Gangster g: experts) {
			expertsInCity.remove(g);
			expertsInHospital.add(g);
			g.hostpitalize();
		}		
	}
	
	public String toString() {
		
		StringBuilder builder = new StringBuilder();
		String lineSep = System.lineSeparator();
		
		builder.append("[" + name + "] {" + lineSep);
		for(CardAction a: actionsInHand){
			builder.append("\t" + a + lineSep);
		}
		builder.append("\t Left Experts: " + getNumOfLeftExperts() + lineSep);
		builder.append("\t Left Occasionals: " + getNumOfLeftOccasionals() + lineSep);
		builder.append("\t In Hospital: " + getNumOfExpertsInHospital() + lineSep);
		builder.append("\t In Prison: " + getNumOfExpertsInPrison() + lineSep);
		builder.append("}");
		
		return builder.toString();
	}
}
