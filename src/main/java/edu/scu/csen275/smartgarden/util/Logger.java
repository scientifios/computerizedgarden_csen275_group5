package edu.scu.csen275.smartgarden.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Centralized logging system for the Smart Garden application.
 * Thread-safe singleton implementation with file and in-memory logging.
 */
public class Logger {
    private static Logger instance;
    private static final Object lock = new Object();
    
    private final Path logFilePath;
    private final String sessionId;
    private final ConcurrentLinkedQueue<LogEntry> buffer;
    private final List<LogEntry> memoryLog;
    private LogLevel minLogLevel;
    private BufferedWriter writer;
    
    // API logging support - write to log.txt when API mode is enabled
    private static BufferedWriter apiLogWriter;
    private static boolean apiModeEnabled = false;
    private static final Object apiLogLock = new Object();
    
    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int BUFFER_FLUSH_SIZE = 10;
    
    /**
     * Private constructor for singleton pattern.
     */
    private Logger() {
        this.sessionId = generateSessionId();
        this.buffer = new ConcurrentLinkedQueue<>();
        this.memoryLog = new ArrayList<>();
        this.minLogLevel = LogLevel.INFO;
        
        // Create logs directory if it doesn't exist
        try {
            Path logsDir = Paths.get("logs");
            if (!Files.exists(logsDir)) {
                Files.createDirectories(logsDir);
            }
            
            this.logFilePath = logsDir.resolve("garden_" + sessionId + ".log");
            this.writer = Files.newBufferedWriter(logFilePath, 
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            
            logHeader();
        } catch (IOException e) {
            System.err.println("Failed to initialize logger: " + e.getMessage());
            throw new RuntimeException("Logger initialization failed", e);
        }
    }
    
    /**
     * Gets the singleton instance of the Logger.
     */
    public static Logger getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new Logger();
                }
            }
        }
        return instance;
    }
    
    /**
     * Logs a message with specified level and category.
     */
    public void log(LogLevel level, String category, String message) {
        if (level.ordinal() < minLogLevel.ordinal()) {
            return; // Skip messages below minimum level
        }
        
        LogEntry entry = new LogEntry(LocalDateTime.now(), level, category, message);
        buffer.add(entry);
        memoryLog.add(entry);
        
        // Also write to log.txt if API mode is enabled (for API monitoring)
        if (apiModeEnabled) {
            writeToApiLog(entry);
        }
        
        // Flush if buffer is full
        if (buffer.size() >= BUFFER_FLUSH_SIZE) {
            flush();
        }
    }
    
    /**
     * Writes a log entry to the API log.txt file (when API mode is enabled).
     */
    private static void writeToApiLog(LogEntry entry) {
        synchronized (apiLogLock) {
            try {
                if (apiLogWriter != null) {
                    apiLogWriter.write(entry.toFileFormat());
                    apiLogWriter.newLine();
                    apiLogWriter.flush();
                }
            } catch (IOException e) {
                // Silently fail - don't disrupt logging if log.txt write fails
            }
        }
    }
    
    /**
     * Logs an INFO level message.
     */
    public void info(String category, String message) {
        log(LogLevel.INFO, category, message);
    }
    
    /**
     * Logs a WARNING level message.
     */
    public void warning(String category, String message) {
        log(LogLevel.WARNING, category, message);
    }
    
    /**
     * Logs an ERROR level message.
     */
    public void error(String category, String message) {
        log(LogLevel.ERROR, category, message);
    }
    
    /**
     * Logs a DEBUG level message.
     */
    public void debug(String category, String message) {
        log(LogLevel.DEBUG, category, message);
    }
    
    /**
     * Logs an exception with stack trace.
     */
    public void logException(String category, String message, Exception e) {
        error(category, message + ": " + e.getMessage());
        error(category, "Stack trace: " + getStackTrace(e));
    }
    
    /**
     * Flushes buffered log entries to file.
     */
    public synchronized void flush() {
        try {
            while (!buffer.isEmpty()) {
                LogEntry entry = buffer.poll();
                if (entry != null) {
                    writer.write(entry.toFileFormat());
                    writer.newLine();
                }
            }
            writer.flush();
        } catch (IOException e) {
            System.err.println("Failed to flush log buffer: " + e.getMessage());
        }
    }
    
    /**
     * Gets recent log entries (last N entries).
     */
    public List<LogEntry> getRecentLogs(int count) {
        int size = memoryLog.size();
        int fromIndex = Math.max(0, size - count);
        return new ArrayList<>(memoryLog.subList(fromIndex, size));
    }
    
    /**
     * Gets all log entries in memory.
     */
    public List<LogEntry> getAllLogs() {
        return new ArrayList<>(memoryLog);
    }
    
    /**
     * Filters logs by category.
     */
    public List<LogEntry> filterByCategory(String category) {
        return memoryLog.stream()
            .filter(entry -> entry.category().equals(category))
            .toList();
    }
    
    /**
     * Filters logs by level.
     */
    public List<LogEntry> filterByLevel(LogLevel level) {
        return memoryLog.stream()
            .filter(entry -> entry.level() == level)
            .toList();
    }
    
    /**
     * Sets the minimum log level to record.
     */
    public void setMinLogLevel(LogLevel level) {
        this.minLogLevel = level;
        info("Logger", "Minimum log level set to: " + level);
    }
    
    /**
     * Enables API logging mode - writes all logs to log.txt file as well as normal log file.
     * This allows API monitoring scripts to see all system responses in one file.
     * 
     * @param apiLogFile Path to the log.txt file (typically "log.txt" in project root)
     */
    public static void enableApiLogging(Path apiLogFile) {
        synchronized (apiLogLock) {
            try {
                // Write header to log.txt
                apiLogWriter = Files.newBufferedWriter(apiLogFile, 
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                
                // Write separator for new session
                apiLogWriter.write("\n");
                apiLogWriter.write("=====================================");
                apiLogWriter.newLine();
                apiLogWriter.write("Smart Garden Simulation API Log");
                apiLogWriter.newLine();
                apiLogWriter.write("Session Started: " + LocalDateTime.now().format(TIME_FORMATTER));
                apiLogWriter.newLine();
                apiLogWriter.write("=====================================");
                apiLogWriter.newLine();
                apiLogWriter.flush();
                
                apiModeEnabled = true;
            } catch (IOException e) {
                System.err.println("Warning: Failed to enable API logging: " + e.getMessage());
                apiModeEnabled = false;
            }
        }
    }
    
    /**
     * Disables API logging and closes the log.txt file.
     */
    public static void disableApiLogging() {
        synchronized (apiLogLock) {
            try {
                if (apiLogWriter != null) {
                    apiLogWriter.flush();
                    apiLogWriter.close();
                    apiLogWriter = null;
                }
                apiModeEnabled = false;
            } catch (IOException e) {
                System.err.println("Error closing API log: " + e.getMessage());
            }
        }
    }
    
    /**
     * Closes the logger and releases resources.
     */
    public void close() {
        flush();
        try {
            info("Logger", "Closing log session: " + sessionId);
            flush();
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing logger: " + e.getMessage());
        }
    }
    
    /**
     * Generates a unique session ID based on timestamp.
     */
    private String generateSessionId() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }
    
    /**
     * Writes log file header.
     */
    private void logHeader() {
        try {
            writer.write("=====================================");
            writer.newLine();
            writer.write("Smart Garden Simulation Log");
            writer.newLine();
            writer.write("Session ID: " + sessionId);
            writer.newLine();
            writer.write("Started: " + LocalDateTime.now().format(TIME_FORMATTER));
            writer.newLine();
            writer.write("=====================================");
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.err.println("Failed to write log header: " + e.getMessage());
        }
    }
    
    /**
     * Converts exception stack trace to string.
     */
    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append("; ");
        }
        return sb.toString();
    }
    
    /**
     * Represents a single log entry.
     */
    public record LogEntry(LocalDateTime timestamp, LogLevel level, String category, String message) {
        public String toFileFormat() {
            return String.format("[%s] %-7s [%-15s] %s",
                timestamp.format(TIME_FORMATTER),
                level.name(),
                category,
                message);
        }
        
        public String toDisplayFormat() {
            return String.format("[%s] %s: %s",
                timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                category,
                message);
        }
    }
    
    /**
     * Log level enumeration.
     */
    public enum LogLevel {
        DEBUG,
        INFO,
        WARNING,
        ERROR
    }
}

