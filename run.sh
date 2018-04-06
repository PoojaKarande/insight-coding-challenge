#!/usr/bin/env bash

javac -cp ./src/Sessionization.java ./src/LogProcessor.java ./src/LogReader.java ./src/LogWriter.java ./src/Sessionization.java ./src/UserLog.java

java -cp ./src Sessionization
