/**
 *  Log.java
 *  Copyright 2010 by Michael Peter Christen
 *  First released 22.4.2010 at http://yacy.net
 *  
 *  This file is part of YaCy Content Integration
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program in the file COPYING.LESSER.
 *  If not, see <http://www.gnu.org/licenses/>.
 */

package net.yacy.cider.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * the log runner de-synchronizes concurrent threads that share the same logger.
 * in concurrent environments it can often be seen that a logger synchronizes
 * all threads at the point where a logging output is done.
 * This class provides a concurrent access to the log4j logger. A separate
 * logging thread reads from a logging queue and writes messages from that queue.
 * 
 * @author m.christen
 *
 */
public class Log {

    
    protected final static logEntry poison = new logEntry();
    protected final static BlockingQueue<logEntry> logQueue = new LinkedBlockingQueue<logEntry>();
    private   static logRunner logRunnerThread = null;
    private   static final Logger thislog = Logger.getLogger(FileUtils.class.getName());
    
    public final static void startRunner() {
        logRunnerThread = new logRunner();
        logRunnerThread.start();
    }

    public final static void stopRunner() {
        if (logRunnerThread == null || !logRunnerThread.isAlive()) return;
        try {
            logQueue.put(poison);
            logRunnerThread.join(1000);
        } catch (InterruptedException e) {
        }
    }
    
    public static final void logInfo(final Logger logger, final String message) {
        enQueueLog(logger, Level.INFO, message);
    }
    
    public static final void logInfo(final Logger logger, final String message, final Throwable thrown) {
        enQueueLog(logger, Level.INFO, message, thrown);
    }
    
    public static final void logDebug(final Logger logger, final String message) {
        enQueueLog(logger, Level.DEBUG, message);
    }
    
    public static final void logDebug(final Logger logger, final String message, final Throwable thrown) {
        enQueueLog(logger, Level.DEBUG, message, thrown);
    }
    
    public static final void logWarning(final Logger logger, final String message) {
        enQueueLog(logger, Level.WARN, message);
    }
    
    public static final void logWarning(final Logger logger, final String message, final Throwable thrown) {
        enQueueLog(logger, Level.WARN, message, thrown);
    }
    
    public static final void logSevere(final Logger logger, final String message) {
        enQueueLog(logger, Level.FATAL, message);
    }
    
    public static final void logSevere(final Logger logger, final String message, final Throwable thrown) {
        enQueueLog(logger, Level.FATAL, message, thrown);
    }
    
    public final static void logException(final Throwable thrown) {
        enQueueLog(thislog, Level.WARN, thrown.getMessage(), thrown);
    }
    
    public final static void logException(final Logger logger, final Throwable thrown) {
        enQueueLog(logger, Level.WARN, thrown.getMessage(), thrown);
    }
    
    
    private final static void enQueueLog(final Logger logger, final Level level, final String message) {
        if (logRunnerThread == null || !logRunnerThread.isAlive()) {
            logger.log(level, message);
        } else {
            try {
                logQueue.put(new logEntry(logger, level, message));
            } catch (InterruptedException e) {
                logger.log(level, message);
            }
        }
    }
    
    private final static void enQueueLog(final Logger logger, final Level level, final String message, final Throwable thrown) {
        if (logRunnerThread == null || !logRunnerThread.isAlive()) {
            logger.log(level, message, thrown);
        } else {
            try {
                logQueue.put(new logEntry(logger, level, message, thrown));
            } catch (InterruptedException e) {
                logger.log(level, message, thrown);
            }
        }
    }
    
    protected final static class logEntry {
        public final Logger logger;
        public final Level level;
        public final String message;
        public final Throwable thrown;
        public logEntry(final Logger logger, final Level level, final String message, final Throwable thrown) {
            this.logger = logger;
            this.level = level;
            this.message = message;
            this.thrown = thrown;
        }
        public logEntry(final Logger logger, final Level level, final String message) {
            this(logger, level, message, null);
        }
        public logEntry() {
            this(null, null, null, null);
        }
    }
    
    protected final static class logRunner extends Thread {
        public logRunner() {
            super("LogRunner");
        }
        
        public void run() {
            logEntry entry;
            try {
                while ((entry = logQueue.take()) != poison) {
                    if (entry.logger != null) {
                        if (entry.thrown == null) {
                            entry.logger.log(entry.level, entry.message);
                        } else {
                            entry.logger.log(entry.level, entry.message, entry.thrown);
                        }
                    }
                }
            } catch (InterruptedException e) {}
            
        }
    }
}
