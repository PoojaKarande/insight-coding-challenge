# Insight Data Science - Coding Challenge April 2017

Java solution for Insight Data Engineering coding challenge - Edgar Analytics

The challenge is to build a scalable pipeline to read EDGAR logs and calculate duration of user activity and number of documents requests made by a particular user.

# Description
The main idea was to use a HashMap to store each active user's IP address and first request timestamp, last request timestamp and number of requests made. To handle session expiry, I maintained a TreeMap of timestamp vs an array of latest IP addresses who made request on that timestamp. This allows quick access to all the IP addresses of expired session.

I decided to use multi threading to perform read, process and write output logs. I also maintained LinkedBlockingQueue between each of these threads.

# Assumptions
1. Logs will be in chronological order (out of order data will be ignored).
2. All the rows in the log will have same pattern as the header. Any row with less or more values than mentioned in the header will be ignored.
3. Two IP address are same if they are a perfect match

# Execution
```
./run.sh
```
# Test

I have added following test cases
1. Sample data provided in the question
2. Ignore chronologically unordered data
3. Ignore incomplete logs
4. Log parsing will happen depending on the header content

Command to run test case
```
cd insight_testsuite
./run_tests.sh
```
