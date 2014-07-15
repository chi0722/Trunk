package control.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import model.constant.CITY_ID;
import model.impl.Boss;

public class Snapshot {

	List<CITY_ID> actionCids;
	String bossName;
	boolean protectEnabled;
	int id;
	int expertsInCity[];
	int occasionalsInCity[];
	int numOfCities;
	int expertsInHand;
	int occasionalsInHand;
	int expertsInPrisonDay1, expertsInPrisonDay2;
	int expertsInHospital;
	int rewards;
	
	public Snapshot(int numOfUsers, int id) {
		
		numOfCities = 2 * numOfUsers;
		bossName = "Untitiled";
		protectEnabled = false;
		this.id = id;
		
		actionCids = new ArrayList<CITY_ID>();
		// Let index = numOfCities be the protector
		expertsInCity = new int[numOfCities + 1];
		occasionalsInCity = new int[numOfCities + 1];
		
		// Occasionals cannot be reused
		occasionalsInHand = Boss.MAX_NUM_OF_OCCASONALS;
		nextHand(Boss.MAX_NUM_OF_EXPERTS, 0, 0);
	}
	
	public String getBossName() { return bossName; }
	public int getID() { return id; }
	public int getExpertsInCity(int cid) { return expertsInCity[cid]; }
    public int getOccasionalsInCity(int cid) { return occasionalsInCity[cid]; }
    public int getGangstersIncity(int cid) { return expertsInCity[cid] + occasionalsInCity[cid]; }
    //public int getExpertsInProtector() { return expertsInCity[numOfCities]; }
    //public int getOccasionalsInProtector() { return occasionalsInCity[numOfCities]; }
    public int getExpertsInHand() { return expertsInHand; }
    public int getOccasionalsInHand() { return occasionalsInHand; }
    public int getExpertsInPrisonDay1() { return expertsInPrisonDay1; }
    public int getExpertsInPrisonDay2() { return expertsInPrisonDay2; }
    public int getExpertsInHospital() { return expertsInHospital; }
    public int getReward() { return rewards; }
    public boolean getProtectEnabled() { return protectEnabled; }
    
    /*public void occupyProtector(int numOfExperts, int numOfOccasionals) {
    
        occupyCity(numOfCities, numOfExperts, numOfOccasionals);
    }*/
    
	public void occupyCity(int cid, int numOfExperts, int numOfOccasionals) {

		expertsInCity[cid] += numOfExperts;
		occasionalsInCity[cid] += numOfOccasionals;
		expertsInHand -= numOfExperts;
		occasionalsInHand -= numOfOccasionals;

        /* protector can be put gangsters after any city has been put. */
		if (numOfExperts != 0)
		    protectEnabled = true;
	}
	
	public void nextHand(int numOfExpertsInHand,
						 int numOfExpertsInHospital,
						 int numOfExpertsInPrison /* day1 + day2 */) {
	      
        protectEnabled = false;
		Arrays.fill(expertsInCity, 0);
		Arrays.fill(occasionalsInCity, 0);
		actionCids.removeAll(actionCids);

        expertsInHand = numOfExpertsInHand;
		expertsInHospital = numOfExpertsInHospital;
		expertsInPrisonDay2 = expertsInPrisonDay1;
		expertsInPrisonDay1 = numOfExpertsInPrison - expertsInPrisonDay2;
	}
	
	public void reward(int reward) {
        
	    rewards += reward;
	}
	
	public String toString() {
		
		StringBuilder builder = new StringBuilder();
		String lineSep = System.lineSeparator();
		
		builder.append("\tExeprt# = " + expertsInHand + lineSep);
		builder.append("\tOccasional# = " + occasionalsInHand + lineSep);
		builder.append("\tHosptial# = " + expertsInHospital + lineSep);
		builder.append("\tPrison# = " + expertsInPrisonDay1 + lineSep);
		builder.append("\t$$ Reward $$ = " + rewards);
		
		return builder.toString();
	}
}
