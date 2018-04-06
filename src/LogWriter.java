import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Queue;
/**
 * Reads expired logs from expiredEvent
 * Writes expired Userlog to ouput.txt file
 * 
 * @author Pooja Karande
 * 
 */
public class LogWriter implements Runnable {
	Queue<UserLog> expiredSessionsQueue = null;
	String outputFileName = null;
	FileOutputStream outputStream = null;
	UserLog expiredEvent = null;
	volatile boolean finished = false;

	public LogWriter(String outputFileName, Queue<UserLog> eventQueue) {
		this.outputFileName = outputFileName;
		this.expiredSessionsQueue = eventQueue;
	}

	@Override
	public void run() {
		try {
			File yourFile = new File(outputFileName);
			yourFile.createNewFile(); 
			// if file already exists will do nothing 
			//outputStream = new FileOutputStream(yourFile, false);
			PrintWriter writer = new PrintWriter(yourFile);
			while(!finished) {
				while ((expiredEvent = expiredSessionsQueue.poll()) != null) {
					if (expiredEvent.getIpAddress() == null) {
						finished = true;
					} else {
						writer.println(expiredEvent.getString());
					}
				}
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}