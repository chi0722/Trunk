package model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import model.constant.ACTION_TYPE;
import model.constant.CITY_ID;
import model.impl.Boss;
import model.impl.Gangster;

public abstract class CardAction implements Serializable {

	private static final long serialVersionUID = 1L;

	protected ACTION_TYPE type;
	protected CITY_ID cid;
	private int hashcode;
	
	public CardAction(ACTION_TYPE type, CITY_ID cid) {
		
		this.type = type;
		this.cid = cid;
		hashcode = getCid().ordinal() * ACTION_TYPE._TOTAL.ordinal() + getType().ordinal();
	}

	public abstract void execute(Map<Boss, List<Gangster>> gangsterMap);
	
	public ACTION_TYPE getType() {
		
		return type;
	}
	
	public CITY_ID getCid() {
		
		return cid;
	}
	
	protected void filterOccasionals(List<Gangster> gangsters){
		
		Gangster[] tmp = gangsters.toArray(
							new Gangster[gangsters.size()]); 
		for(Gangster g: tmp){
			switch(g.getType()){
				case OCCASIONAL:
					gangsters.remove(g);
					break;
				default:
					break;
			}
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if(obj instanceof CardAction) {
			CardAction action = (CardAction) obj;
			return (getCid() == action.getCid() && getType() == action.getType());
		} else
			return false;
	}
	
	@Override
	public int hashCode() {
		
		return hashcode;
	}
	
	@Override
	public String toString(){
		
		return "[" + cid + "] " + type;
	}
}
