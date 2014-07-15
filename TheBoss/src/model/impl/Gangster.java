package model.impl;

import model.constant.GANGSTER_TYPE;

public class Gangster {
	
	private GANGSTER_TYPE type;
	private int pauseHands;
	
	public Gangster(GANGSTER_TYPE type){
		
		this.type = type;
		pauseHands = 0;
	}
	
	public GANGSTER_TYPE getType(){ return type; }	
	public void imprison(){ pauseHands = 2;	}
	public void hostpitalize(){ pauseHands = 1;	}
	public boolean inSanction(){ return (pauseHands > 0); }
	
	public void impose(){
		
		assert(pauseHands > 0);
		pauseHands--;
	}	
}
