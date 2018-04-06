import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.PriorityBlockingQueue;
/**
 * Reads log from logQueue
 * Processes and checks for expiry
 * Puts expired log to expiredSessionsQueue
 * 
 * @author Pooja Karande
 * 
 */
public class LogProcessor implements Runnable {

	Queue<UserLog> logQueue = null;
	Queue<UserLog> expiredSessionsQueue = null;
	// Map of ipAddress and corresponding UserLog object
	HashMap<String, UserLog> userMap = null;

	// TreeMap of timestamp and UserLog with corresponding lastRequestTimestamp (Used to check expired user's ipAddress)
	TreeMap<Timestamp, ArrayList<String>> timestampMap = null;
	int inactivityPeriod = 0;
	volatile boolean finished = false;

	public LogProcessor(Queue<UserLog> logQueue, Queue<UserLog> expiredSessionsQueue, int inactivityPeriod) {
		this.logQueue = logQueue;
		this.expiredSessionsQueue = expiredSessionsQueue;
		this.timestampMap = new TreeMap<>();
		this.userMap = new HashMap<>();
		this.inactivityPeriod = inactivityPeriod;
	}

	@Override
	public void run() {
		UserLog newLog = null;
		UserLog oldLog = null;
		Timestamp latestTS = null;
		ArrayList<String> tempList = null;
		
		try {
			while(!finished) {
				while ((newLog = logQueue.poll()) != null) {
					String ipAddress = newLog.getIpAddress();
					// If null user log received push remaining logs in the userMap to the expiredQueue
					if (ipAddress == null) {
						Iterator it = userMap.keySet().iterator();
					    PriorityBlockingQueue<UserLog> tempQueue = new PriorityBlockingQueue<>(1000,new Comparator<UserLog>() {
							@Override
							public int compare(UserLog o1, UserLog o2) {
								// order remaining logs by first request timestamp
								return o1.getFirstRequestTimestamp().compareTo(o2.getFirstRequestTimestamp()); 
							}
						});
						while(it.hasNext()) {
							ipAddress = (String) it.next();
							tempQueue.add(userMap.get(ipAddress));
						}
						while((oldLog = tempQueue.poll()) != null) {
							expiredSessionsQueue.add(oldLog);
						}
						expiredSessionsQueue.add(newLog);
						finished = true;
						break;
					}

					Timestamp currentTS = newLog.getLastRequestTimestamp();
					// Ignore chronologically unordered data
					if (latestTS != null && currentTS.before(latestTS)) {
						continue;
					}
					
					// If previous session for the user has not expired
					if (userMap.containsKey(ipAddress)) {
						// get the previous session
						oldLog = userMap.get(ipAddress);	
						if (oldLog.getLastRequestTimestamp().before(currentTS)) {
							tempList = timestampMap.get(oldLog.getLastRequestTimestamp());
							tempList.remove(ipAddress);
							timestampMap.put(oldLog.getLastRequestTimestamp(), tempList);
							
							if (timestampMap.containsKey(currentTS)) {
								tempList = timestampMap.get(currentTS);
								tempList.add(newLog.getIpAddress());
							} else {
								tempList = new ArrayList<String>();
								tempList.add(newLog.getIpAddress());
							}
							timestampMap.put(currentTS, tempList);
						}
						// Add new event details to previous session
						oldLog.merge(newLog);
						userMap.put(ipAddress, oldLog);
					} else {
						// Add user to user Map
						userMap.put(newLog.getIpAddress(), newLog);
						if (timestampMap.containsKey(currentTS)) {
							tempList = timestampMap.get(currentTS);
							tempList.add(newLog.getIpAddress());
						} else {
							tempList = new ArrayList<String>();
							tempList.add(newLog.getIpAddress());
						}
						timestampMap.put(currentTS, tempList);
					}
					// time changed
					if (latestTS != null && latestTS.before(currentTS)) {
						checkExpiredEvents(currentTS);
					}
					latestTS = currentTS;
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void checkExpiredEvents(Timestamp latestTS) {
		// Get timestamp which are older than inactivity period
		SortedMap<Timestamp, ArrayList<String>> headMap = timestampMap.headMap(Timestamp.from(latestTS.toInstant().minusSeconds(inactivityPeriod)));
		for(Entry<Timestamp, ArrayList<String>> entry : headMap.entrySet()) {
			  ArrayList<String> list = entry.getValue();
	        	for(int j =0 ; j< list.size() ; j++) {
	        		UserLog userLog = userMap.get(list.get(j));
					// Pass logs of inactive user to expired queue
	        		expiredSessionsQueue.add(userLog);
					userMap.remove(list.get(j));
				}
			}
		// Delete all expired records from timestamp TreeMap
		headMap.clear();
	}
}