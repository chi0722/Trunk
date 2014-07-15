package util;

import io.MessageIO;

import java.io.IOException;

public interface MessageListener {

	public void handleMessage(MessageActionEvent event);
	public void handleIOException(MessageIO src, IOException e);
}
