package io;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import util.Message;
import util.MessageType;

public class MessageIO {

	private Socket socket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	
	public MessageIO(Socket socket) throws IOException {
	
		this.socket = socket;
		oos = new ObjectOutputStream(socket.getOutputStream());
		ois = new ObjectInputStream(socket.getInputStream());
	}
		
	public void writeMessage(MessageType type, Object payload) throws IOException {
		
	    Message msg = new Message(type, payload);
	    System.err.println("Send" + msg.toString());
		oos.writeObject(msg);
	}
	
	public Message readMessage() throws IOException, ClassNotFoundException {
		
		return (Message)ois.readObject();
	}
	
	public Socket getSocket() {
		
		return socket;
	}
	
	public void flush() throws IOException {
	
		oos.flush();
	}
	
	public void close() throws IOException {
	
		oos.close();
		ois.close();
		socket.close();
	}
}
