package util;

import java.io.Serializable;

public class Message implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final MessageType type;
	private final Object payload;
	
	public Message(MessageType type) {
		
		this(type, null);
	}
	
	public Message(MessageType type, Object payload) {
		
		this.type = type;
		this.payload = payload;
	}
	
	public MessageType getType() { return type; }
	public Object getPayload() { return payload; }
	
	public String toString() {
		
		if(payload == null)
			return "Msg(" + type + ")";
		else
			return "Msg(" + type + "| " + payload.toString() + ")";
	}
}
