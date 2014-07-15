package control;

import io.MessageIO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import util.MessageActionEvent;
import util.MessageListener;
import util.MessageType;

public abstract class Unit implements MessageListener {

	protected List<UnitListener> listeners;
	protected State currentState;
	
	public Unit(){

		listeners = new ArrayList<UnitListener>();
	}
	
	protected void stateChange(State targetState) {
		
		System.out.println("State changed to " + targetState);
		currentState = targetState;
		currentState.handle();
	}

	public boolean addUnitListener(UnitListener listener){
		
		return listeners.add(listener);
	}

	public boolean removeUnitListener(UnitListener listener){
		
		return listeners.remove(listener);
	}
	
	public void fireUnitEvent(MessageType type, String ret){
		
		for(UnitListener listener: listeners)
			listener.report(type, ret);
	}

	@Override
	public synchronized void handleMessage(MessageActionEvent event) {
		
		currentState.transit(event);
	}

	@Override
	public void handleIOException(MessageIO src, IOException e) {
		/* do nothing */
	}
}
