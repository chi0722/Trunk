package logging;

public class Logger {

	public static boolean isLogging = true;
	private static Logger instance = new Logger(); 
	
	private Logger(){
		
	}
	
	public Logger getInstance(){
		
		return instance;
	}
	
	public void log(LOG_TYPE type, String msg){
		
		if(!isLogging)
			return;
		
		switch(type){
			case DEBUG:
				System.out.println(msg);
				break;
			case WARNING:
				System.err.println("[WARNING] " + msg);
				break;
			case ERROR:
				System.err.println("[ERROR] " + msg);
				break;
		}
	}
}
