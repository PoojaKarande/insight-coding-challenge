import java.sql.Timestamp;
import java.text.SimpleDateFormat;
/**
 * Class to store a user's session
 * 
 * @author Pooja Karande
 * 
 */
public class UserLog {
	private String ipAddress;
	private Timestamp firstRequestTimestamp;
	private Timestamp lastRequestTimestamp;
	private Integer countOfRequests = 0;
	
	// Constructor to create NULL log
	public UserLog() {
		this.firstRequestTimestamp =  new Timestamp(System.currentTimeMillis());
		this.lastRequestTimestamp =  new Timestamp(System.currentTimeMillis());
		this.countOfRequests = 0;
	}

	// Constructor to create UserLog from string
	public UserLog(String ipAddress, String date, String time) {
		this.ipAddress = ipAddress;
		this.firstRequestTimestamp = Timestamp.valueOf(date + " " + time);
		this.lastRequestTimestamp = Timestamp.valueOf(date + " " + time);
		this.countOfRequests = 1;
	}

	// Method to print expired log to output file
	public String getString() {
		String output = ipAddress + "," + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(firstRequestTimestamp)	 + "," + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(lastRequestTimestamp) + "," 
				+ ((1000 + lastRequestTimestamp.toInstant().toEpochMilli() - firstRequestTimestamp.toInstant().toEpochMilli())/1000) + ","
				+ countOfRequests.toString();
		return output;
	}

	// Method to merge UserLog objects if previous user session has not expired
	public void merge(UserLog expiredEvent) {
		this.lastRequestTimestamp = expiredEvent.firstRequestTimestamp;
		this.countOfRequests += 1;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public Timestamp getFirstRequestTimestamp() {
		return firstRequestTimestamp;
	}

	public Timestamp getLastRequestTimestamp() {
		return lastRequestTimestamp;
	}
}