package control;

import java.util.HashMap;
import java.util.Map;

import util.MessageAction;
import util.MessageActionEvent;
import util.MessageType;

public abstract class State {

	protected Map<MessageType, MessageAction> actionMap;
	protected String name;
//	protected MessageAction noAction = new MessageAction() {
//
//		@Override
//		public void actionPerformed(MessageActionEvent event) {
//			/* do nothing */
//		}		
//	};
	
	public State(String name) {
		
		actionMap = new HashMap<MessageType, MessageAction>();
		this.name = name;
		initTransitionActions();
	}
	
	public abstract void handle();
	protected abstract void initTransitionActions();
	
	public void transit(MessageActionEvent event) {
	
		MessageType msgType = event.getMessage().getType();
		
		System.out.println("Recieve " + event.getMessage() + "@State: " + name);
		if(actionMap.containsKey(msgType))
			actionMap.get(msgType).actionPerformed(event);
		else
			throw new RuntimeException(this.toString() + ": " + 
									   "Message type not found.");
	}
	
	public String toString() {
		
		return name + "-State";
	}
}
