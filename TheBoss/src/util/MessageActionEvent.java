package util;

public class MessageActionEvent {

	private Object source;
	private Message message;
	
	public MessageActionEvent(Object source, Message event) {
		
		this.source = source;
		this.message = event;
	}
	
	public Object getSource() { return source; }
	public Message getMessage(){ return message; }
}
