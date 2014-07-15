package io;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import util.Message;
import util.MessageActionEvent;
import util.MessageListener;
import util.MessageType;

public class MessageIOHandler extends Thread {

    private BlockingQueue<MessageActionEvent> eventQ;
	private MessageListener listener;
	private MessageIO mio;
	
	public MessageIOHandler(MessageIO mio) {
		
		this.mio = mio;
	}
	
	/* support non-direct-call to handler */
	public MessageIOHandler(MessageIO mio, BlockingQueue<MessageActionEvent> eventQ) {
	    
	    this(mio);
	    this.eventQ = eventQ;
	}
	
	public void setMessageListener(MessageListener listener) {
	
		this.listener = listener;
	}
	
	public void run() {
			
		try {
			Message msg = null;
			while((msg=mio.readMessage()) != null) {
			    
			    /* early break; */
			    if (msg.getType() == MessageType.EXCEPTION)
			        throw new IOException();
			    
			    if (eventQ != null)
			        eventQ.put(new MessageActionEvent(mio, msg));
			    else
			        listener.handleMessage(new MessageActionEvent(mio, msg));
			}
		} catch(IOException e) {
			System.err.println(e);
			listener.handleIOException(mio, e);
		} catch(ClassNotFoundException | InterruptedException e) {
			e.printStackTrace();
        } finally {            
            try {
                mio.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
	}
}
