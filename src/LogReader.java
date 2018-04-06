import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Queue;
import java.util.Scanner;
/**
 * Reads input file and parses each line into UserLog object
 * Puts each object into eventQueue
 * 
 * @author Pooja Karande
 * 
 */
public class LogReader implements Runnable {
	private Queue<UserLog> logQueue;
	String fileName;
	FileInputStream inputStream = null;
	Scanner sc = null;

	public LogReader(String fileName, Queue<UserLog> logQueue) {
		this.fileName = fileName;
		this.logQueue = logQueue;
	}

	@Override
	public void run() {
		try {
		    inputStream = new FileInputStream(fileName);
		    sc = new Scanner(inputStream, "UTF-8");
		    // Parse header and store as Map of name -> index
		    String header = sc.nextLine();
		    String[] headerArray = header.split(",");
		    HashMap<String, Integer> headerMap = new HashMap<>();
		    
		    for (int i = 0; i < headerArray.length; i++) {
				headerMap.put(headerArray[i], i);
			}
		    
		    // Read log from file, create UserLog object and insert in eventQueue
		    while (sc.hasNextLine()) {
		        String line = sc.nextLine();
				String arr[] =line.split(",");
				if(arr.length == headerArray.length - 1) {
			        UserLog log = new UserLog(arr[headerMap.get("ip")], arr[headerMap.get("date")], arr[headerMap.get("time")]);
			        if (log.getIpAddress() != null) {
			        	logQueue.add(log);
			        }					
				}
		    }
		    
		    // Notify Processor thread the end of file by adding empty UserLog object
		    logQueue.add(new UserLog());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		    if (inputStream != null) {
		        try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		    }
		    sc.close();
		}
	}
	
}