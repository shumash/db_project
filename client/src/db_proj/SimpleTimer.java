package db_proj;

import java.util.Date;

public class SimpleTimer {
    long startTime = new Date().getTime();
    static SimpleTimer singleton = null;

    static void timedLog(String s) {
    	System.out.print(Singleton().getMs() + ": " + s);
    }

    static SimpleTimer Singleton() {
    	if (singleton == null) {
    		singleton = new SimpleTimer();
    	}
    	return singleton;
    }

    SimpleTimer() {
    }

    void start() {
    	startTime = new Date().getTime();
    }

    long getMs() {
    	return new Date().getTime() - startTime;
    }

    void printDone() {
    	System.out.println("done in " + getMs() + "ms");
    }
}
