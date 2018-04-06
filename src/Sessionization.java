import java.io.FileInputStream;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
/**
 * Main class to start Sessionization
 * 
 * @author Pooja Karande
 *
 */
public class Sessionization {
	
	static final int MAX_T = 3;
    // Queue used between reader and processor
    private BlockingQueue<UserLog> logQueue = new LinkedBlockingQueue<>();
    // Queue used between processor and writer
    private BlockingQueue<UserLog> expiredSessionsQueue = new LinkedBlockingQueue<>();
    String inputFileName = null;
    String outputFileName = null;
    int inactivityPeriod = 0;

    public Sessionization(String inputFileName, String outputFileName, int inactivityPeriod) {
		this.inputFileName = inputFileName;
		this.outputFileName = outputFileName;
		this.inactivityPeriod = inactivityPeriod;
	}

	public void start() {
    	try {
    		// Start 3 threads - Reader, processor, writer
    		Thread t1 = new Thread(new LogReader(inputFileName, logQueue));
    		Thread t2 = new Thread(new LogProcessor(logQueue, expiredSessionsQueue, inactivityPeriod));
    		Thread t3 = new Thread(new LogWriter(outputFileName, expiredSessionsQueue));
    		
    		t1.start();
    		t2.start();
    		t3.start();
    		
    		t1.join();
    		t2.join();
    		t3.join();
    		
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }

	public static void main(String[] args) {
		FileInputStream inputStream = null;
		Sessionization obj = null;

		try {
			// Reade inactivity period from file
			inputStream = new FileInputStream("./input/inactivity_period.txt");
		    Scanner sc = new Scanner(inputStream, "UTF-8");
			
		    obj = new Sessionization("./input/log.csv", "./output/sessionization.txt", sc.nextInt());			
			obj.start();

			inputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
