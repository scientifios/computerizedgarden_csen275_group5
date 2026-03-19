package CSEN275Garden.util;

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
 * Application logger with a buffered file sink and an in-memory history.
 * Provides a singleton instance and optional API log mirroring.
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
    
    // When API logging is enabled, entries are also mirrored to a separate file.
    private static BufferedWriter apiLogWriter;
    private static boolean apiModeEnabled = false;
    private static final Object apiLogLock = new Object();
    
    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int BUFFER_FLUSH_SIZE = 10;
    
    /**
     * Initializes a logging session and opens the per-session log file.
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
     * @return singleton logger instance
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
     * Records a log entry if it meets the current minimum level.
     * Entries are buffered for file flush and retained in memory; optionally mirrored for API mode.
     */
    public void log(LogLevel level, String category, String message) {
        if (level.ordinal() < minLogLevel.ordinal()) {
            return; // Skip messages below minimum level
        }
        
        LogEntry entry = new LogEntry(LocalDateTime.now(), level, category, message);
        buffer.add(entry);
        memoryLog.add(entry);
        
        // Mirror entries to the API log when API logging is enabled.
        if (apiModeEnabled) {
            writeToApiLog(entry);
        }
        
        if (buffer.size() >= BUFFER_FLUSH_SIZE) {
            flush();
        }
    }
    
    /**
     * Writes an entry to the API mirror log when API logging is enabled.
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
                // Best-effort mirror write; failures should not disrupt primary logging.
            }
        }
    }
    
    public void info(String category, String message) {
        log(LogLevel.INFO, category, message);
    }
    
    public void warning(String category, String message) {
        log(LogLevel.WARNING, category, message);
    }
    
    public void error(String category, String message) {
        log(LogLevel.ERROR, category, message);
    }
    
    public void debug(String category, String message) {
        log(LogLevel.DEBUG, category, message);
    }
    
    /**
     * Logs an exception message plus a flattened stack trace.
     */
    public void logException(String category, String message, Exception e) {
        error(category, message + ": " + e.getMessage());
        error(category, "Stack trace: " + getStackTrace(e));
    }
    
    /**
     * Drains the buffer and appends entries to the session log file.
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
     * Returns up to the last N in-memory log entries (most recent last).
     */
    public List<LogEntry> getRecentLogs(int count) {
        int size = memoryLog.size();
        int fromIndex = Math.max(0, size - count);
        return new ArrayList<>(memoryLog.subList(fromIndex, size));
    }
    
    /**
     * @return snapshot of the in-memory log history
     */
    public List<LogEntry> getAllLogs() {
        return new ArrayList<>(memoryLog);
    }
    
    /**
     * Returns in-memory entries matching the given category.
     */
    public List<LogEntry> filterByCategory(String category) {
        return memoryLog.stream()
            .filter(entry -> entry.category().equals(category))
            .toList();
    }
    
    /**
     * Returns in-memory entries matching the given level.
     */
    public List<LogEntry> filterByLevel(LogLevel level) {
        return memoryLog.stream()
            .filter(entry -> entry.level() == level)
            .toList();
    }
    
    /**
     * Updates the minimum log level gate for subsequent entries.
     */
    public void setMinLogLevel(LogLevel level) {
        this.minLogLevel = level;
        info("Logger", "Minimum log level set to: " + level);
    }
    
    /**
     * Enables API log mirroring to the given file (in addition to the per-session log file).
     *
     * @param apiLogFile destination path for the API mirror log
     */
    public static void enableApiLogging(Path apiLogFile) {
        synchronized (apiLogLock) {
            try {
                // Open the API mirror writer and write a session header.
                apiLogWriter = Files.newBufferedWriter(apiLogFile, 
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                
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
     * Disables API log mirroring and closes the mirror writer.
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
     * Flushes pending entries and closes the session log writer.
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
     * Generates a timestamp-based session id for file naming.
     */
    private String generateSessionId() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }
    
    /**
     * Writes a session header to the log file.
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
     * Flattens a stack trace into a single-line string.
     */
    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append("; ");
        }
        return sb.toString();
    }
    
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
    
    public enum LogLevel {
        DEBUG,
        INFO,
        WARNING,
        ERROR
    }
}

