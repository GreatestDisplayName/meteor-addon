package com.meteoraddon.addon.utils;

import com.meteoraddon.addon.AddonTemplate;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class TextDebug {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateTimeFormatter FILE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final String LOG_DIR = "C:\\Users\\Abdal\\AppData\\Roaming\\.minecraft\\meteor-addon\\debug_logs";
    private static final String LOG_FILE_PREFIX = "meteor_addon_debug_";
    private static final String LOG_FILE_SUFFIX = ".log";
    private static final int MAX_LOG_FILES = 10;
    private static final int MAX_QUEUE_SIZE = 1000;
    
    private static boolean fileLoggingEnabled = false;
    private static boolean chatLoggingEnabled = true;
    private static boolean consoleLoggingEnabled = true;
    private static boolean asyncLoggingEnabled = true;
    private static boolean coloredConsoleEnabled = true;
    private static DebugLevel minimumLevel = DebugLevel.DEBUG;
    private static String currentLogFile;
    
    // Async logging
    private static final ExecutorService logExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "TextDebug-Logger");
        t.setDaemon(true);
        return t;
    });
    private static final ConcurrentLinkedQueue<LogEntry> logQueue = new ConcurrentLinkedQueue<>();
    private static final AtomicBoolean isWriting = new AtomicBoolean(false);
    private static final AtomicLong totalLogEntries = new AtomicLong(0);
    
    // Log entry for async processing
    private static class LogEntry {
        final String message;
        
        LogEntry(String message) {
            this.message = message;
        }
    }
    
    // Debug levels
    public enum DebugLevel {
        TRACE(Formatting.GRAY, "TRACE", 0),
        DEBUG(Formatting.AQUA, "DEBUG", 1),
        INFO(Formatting.GREEN, "INFO", 2),
        WARN(Formatting.YELLOW, "WARN", 3),
        ERROR(Formatting.RED, "ERROR", 4),
        FATAL(Formatting.DARK_RED, "FATAL", 5);
        
        private final Formatting color;
        private final String name;
        private final int priority;
        
        DebugLevel(Formatting color, String name, int priority) {
            this.color = color;
            this.name = name;
            this.priority = priority;
        }
        
        public Formatting getColor() { return color; }
        public String getName() { return name; }
        public int getPriority() { return priority; }
        
        public boolean shouldLog(DebugLevel minLevel) {
            return this.priority >= minLevel.priority;
        }
    }
    
    // Configuration methods
    public static void setFileLogging(boolean enabled) {
        fileLoggingEnabled = enabled;
        if (enabled) {
            initializeLogFile();
            info("File logging enabled");
        }
    }
    
    public static void setChatLogging(boolean enabled) {
        chatLoggingEnabled = enabled;
        if (enabled) {
            info("Chat logging enabled");
        }
    }
    
    public static void setConsoleLogging(boolean enabled) {
        consoleLoggingEnabled = enabled;
        if (enabled) {
            info("Console logging enabled");
        }
    }
    
    public static void setAsyncLogging(boolean enabled) {
        asyncLoggingEnabled = enabled;
        info("Async logging {}", enabled ? "enabled" : "disabled");
    }
    
    public static void setColoredConsole(boolean enabled) {
        coloredConsoleEnabled = enabled;
        info("Colored console logging {}", enabled ? "enabled" : "disabled");
    }
    
    public static void setMinimumLevel(DebugLevel level) {
        minimumLevel = level;
        info("Minimum debug level set to {}", level.getName());
    }
    
    private static void initializeLogFile() {
        try {
            Path logDir = Paths.get(LOG_DIR);
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }
            
            // Clean up old log files
            cleanupOldLogFiles();
            
            // Create new log file with timestamp
            String timestamp = LocalDateTime.now().format(FILE_TIME_FORMAT);
            currentLogFile = LOG_DIR + "/" + LOG_FILE_PREFIX + timestamp + LOG_FILE_SUFFIX;
            
            // Write header
            try (PrintWriter writer = new PrintWriter(new FileWriter(currentLogFile, false))) {
                writer.println("=== Meteor Addon Debug Log Started at " + LocalDateTime.now().format(TIME_FORMAT) + " ===");
                writer.println("=== Minecraft Version: " + (MinecraftClient.getInstance() != null ? "Unknown" : "Unknown") + " ===");
                writer.println("=== Java Version: " + System.getProperty("java.version") + " ===");
                writer.println();
            }
        } catch (IOException e) {
            AddonTemplate.LOG.error("Failed to initialize log file: {}", e.getMessage());
        }
    }
    
    private static void cleanupOldLogFiles() {
        try {
            Path logDir = Paths.get(LOG_DIR);
            if (!Files.exists(logDir)) return;
            
            Files.list(logDir)
                .filter(path -> path.getFileName().toString().startsWith(LOG_FILE_PREFIX))
                .sorted((p1, p2) -> {
                    try {
                        return Files.getLastModifiedTime(p2).compareTo(Files.getLastModifiedTime(p1));
                    } catch (IOException e) {
                        return 0;
                    }
                })
                .skip(MAX_LOG_FILES)
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        info("Deleted old log file: {}", path.getFileName());
                    } catch (IOException e) {
                        warn("Failed to delete old log file {}: {}", path.getFileName(), e.getMessage());
                    }
                });
        } catch (IOException e) {
            warn("Failed to cleanup old log files: {}", e.getMessage());
        }
    }
    
    // Core logging methods
    public static void log(DebugLevel level, String message, Object... args) {
        // Check minimum level
        if (!level.shouldLog(minimumLevel)) {
            return;
        }
        
        String formattedMessage = formatMessage(message, args);
        String timestamp = LocalDateTime.now().format(TIME_FORMAT);
        String logEntry = String.format("[%s] [%s] [%s] %s", timestamp, level.getName(), Thread.currentThread().getName(), formattedMessage);
        
        // Console logging
        if (consoleLoggingEnabled) {
            switch (level) {
                case FATAL:
                case ERROR:
                    AddonTemplate.LOG.error(formattedMessage);
                    break;
                case WARN:
                    AddonTemplate.LOG.warn(formattedMessage);
                    break;
                case INFO:
                    AddonTemplate.LOG.info(formattedMessage);
                    break;
                case DEBUG:
                case TRACE:
                    AddonTemplate.LOG.debug(formattedMessage);
                    break;
            }
        }
        
        // Chat logging
        if (chatLoggingEnabled && MinecraftClient.getInstance().player != null) {
            String chatMessage = formatChatMessage(level, formattedMessage);
            ChatUtils.sendMsg(Text.literal(chatMessage));
        }
        
        // File logging
        if (fileLoggingEnabled) {
            if (asyncLoggingEnabled) {
                // Async logging
                LogEntry entry = new LogEntry(logEntry);
                if (logQueue.size() < MAX_QUEUE_SIZE) {
                    logQueue.offer(entry);
                    totalLogEntries.incrementAndGet();
                    processLogQueue();
                } else {
                    warn("Log queue full, dropping message: {}", formattedMessage);
                }
            } else {
                // Sync logging
                writeToFile(logEntry);
            }
        }
    }
    
    private static void processLogQueue() {
        if (isWriting.compareAndSet(false, true)) {
            logExecutor.submit(() -> {
                try {
                    while (!logQueue.isEmpty()) {
                        LogEntry entry = logQueue.poll();
                        if (entry != null) {
                            writeToFile(entry.message);
                        }
                    }
                } finally {
                    isWriting.set(false);
                    // Check if new entries were added while processing
                    if (!logQueue.isEmpty()) {
                        processLogQueue();
                    }
                }
            });
        }
    }
    
    // Convenience methods
    public static void trace(String message, Object... args) {
        log(DebugLevel.TRACE, message, args);
    }
    
    public static void debug(String message, Object... args) {
        log(DebugLevel.DEBUG, message, args);
    }
    
    public static void info(String message, Object... args) {
        log(DebugLevel.INFO, message, args);
    }
    
    public static void warn(String message, Object... args) {
        log(DebugLevel.WARN, message, args);
    }
    
    public static void error(String message, Object... args) {
        log(DebugLevel.ERROR, message, args);
    }
    
    public static void fatal(String message, Object... args) {
        log(DebugLevel.FATAL, message, args);
    }
    
    // Specialized logging methods
    public static void module(String moduleName, String message, Object... args) {
        info("[{}] {}", moduleName, formatMessage(message, args));
    }
    
    public static void command(String commandName, String message, Object... args) {
        info("[CMD:{}] {}", commandName, formatMessage(message, args));
    }
    
    public static void hud(String hudName, String message, Object... args) {
        info("[HUD:{}] {}", hudName, formatMessage(message, args));
    }
    
    public static void event(String eventName, String message, Object... args) {
        debug("[EVENT:{}] {}", eventName, formatMessage(message, args));
    }
    
    public static void position(String context, double x, double y, double z) {
        debug("[%s] Position: X=%.2f, Y=%.2f, Z=%.2f", context, x, y, z);
    }
    
    public static void position(String context, int x, int y, int z) {
        debug("[%s] Position: X=%d, Y=%d, Z=%d", context, x, y, z);
    }
    
    public static void blockState(String context, String blockType, int x, int y, int z) {
        debug("[{}] Block: %s at (%d, %d, %d)", context, blockType, x, y, z);
    }
    
    public static void inventory(String context, String item, int slot, int count) {
        debug("[{}] Inventory: %s x%d in slot %d", context, item, count, slot);
    }
    
    public static void timing(String operation, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        debug("[TIMING] {} took {}ms", operation, duration);
    }
    
    // Additional specialized logging methods
    public static void network(String context, String message, Object... args) {
        debug("[NET:{}] {}", context, formatMessage(message, args));
    }
    
    public static void combat(String context, String message, Object... args) {
        info("[COMBAT:{}] {}", context, formatMessage(message, args));
    }
    
    public static void movement(String context, String message, Object... args) {
        trace("[MOVE:{}] {}", context, formatMessage(message, args));
    }
    
    public static void rotation(String context, float yaw, float pitch) {
        trace("[{}] Rotation: Yaw=%.1f, Pitch=%.1f", context, yaw, pitch);
    }
    
    public static void health(String context, float health, float maxHealth) {
        info("[{}] Health: %.1f/%.1f (%.1f%%)", context, health, maxHealth, (health/maxHealth)*100);
    }
    
    public static void memory(String context) {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
        debug("[{}] Memory: %.1fMB/%.1fMB (%.1f%%)", context, 
            usedMemory / 1024.0 / 1024.0, maxMemory / 1024.0 / 1024.0, memoryUsagePercent);
    }
    
    public static void fps(String context) {
        if (MinecraftClient.getInstance() != null) {
            int fps = MinecraftClient.getInstance().getCurrentFps();
            trace("[{}] FPS: {}", context, fps);
        }
    }
    
    public static void chunk(String context, int chunkX, int chunkZ) {
        debug("[{}] Chunk: (%d, %d)", context, chunkX, chunkZ);
    }
    
    public static void entity(String context, String entityType, int count) {
        info("[{}] Entities: {} x{}", context, entityType, count);
    }
    
    public static void config(String context, String key, Object value) {
        debug("[{}] Config: {} = {}", context, key, value);
    }
    
    public static void performance(String operation, long startTimeNanos) {
        long durationMicros = (System.nanoTime() - startTimeNanos) / 1000;
        if (durationMicros > 1000) {
            warn("[PERF] {} took {}ms", operation, durationMicros / 1000.0);
        } else {
            debug("[PERF] {} took {}μs", operation, durationMicros);
        }
    }
    
    public static void startTimer(String operation) {
        debug("[TIMER] Started: {}", operation);
    }
    
    public static void endTimer(String operation, long startTime) {
        timing(operation, startTime);
    }
    
    // Utility methods
    private static String formatMessage(String message, Object... args) {
        if (args.length == 0) return message;
        return String.format(message, args);
    }
    
    private static String formatChatMessage(DebugLevel level, String message) {
        return level.getColor() + "[" + level.getName() + "] " + Formatting.WHITE + message;
    }
    
    private static void writeToFile(String logEntry) {
        if (currentLogFile == null) return;
        try (PrintWriter writer = new PrintWriter(new FileWriter(currentLogFile, true))) {
            writer.println(logEntry);
        } catch (IOException e) {
            AddonTemplate.LOG.error("Failed to write to debug log file: {}", e.getMessage());
        }
    }
    
    // File operations
    public static void clearLogFile() {
        if (currentLogFile != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(currentLogFile, false))) {
                writer.print("");
                info("Debug log file cleared");
            } catch (IOException e) {
                error("Failed to clear debug log file: {}", e.getMessage());
            }
        } else {
            warn("No log file to clear");
        }
    }
    
    public static String getLogFilePath() {
        return currentLogFile != null ? currentLogFile : "No log file initialized";
    }
    
    public static String getLogDirectory() {
        return LOG_DIR;
    }
    
    public static long getTotalLogEntries() {
        return totalLogEntries.get();
    }
    
    public static int getQueueSize() {
        return logQueue.size();
    }
    
    // Status methods
    public static boolean isFileLoggingEnabled() {
        return fileLoggingEnabled;
    }
    
    public static boolean isChatLoggingEnabled() {
        return chatLoggingEnabled;
    }
    
    public static boolean isConsoleLoggingEnabled() {
        return consoleLoggingEnabled;
    }
    
    public static boolean isAsyncLoggingEnabled() {
        return asyncLoggingEnabled;
    }
    
    public static boolean isColoredConsoleEnabled() {
        return coloredConsoleEnabled;
    }
    
    public static DebugLevel getMinimumLevel() {
        return minimumLevel;
    }
    
    public static void printStatus() {
        info("TextDebug Status:");
        info("  File Logging: {}", fileLoggingEnabled ? "Enabled" : "Disabled");
        info("  Chat Logging: {}", chatLoggingEnabled ? "Enabled" : "Disabled");
        info("  Console Logging: {}", consoleLoggingEnabled ? "Enabled" : "Disabled");
        info("  Async Logging: {}", asyncLoggingEnabled ? "Enabled" : "Disabled");
        info("  Colored Console: {}", coloredConsoleEnabled ? "Enabled" : "Disabled");
        info("  Minimum Level: {}", minimumLevel.getName());
        info("  Log Directory: {}", getLogDirectory());
        info("  Current Log File: {}", getLogFilePath());
        info("  Total Entries Logged: {}", getTotalLogEntries());
        info("  Queue Size: {}", getQueueSize());
    }
    
    // Shutdown method
    public static void shutdown() {
        info("Shutting down TextDebug...");
        logExecutor.shutdown();
        try {
            if (!logExecutor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                warn("Log executor did not terminate gracefully");
                logExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logExecutor.shutdownNow();
        }
        info("TextDebug shutdown complete");
    }
    
    // Exception logging method
    public static void exception(String context, Exception e) {
        error("[{}] Exception: {} - {}", context, e.getClass().getSimpleName(), e.getMessage());
        if (consoleLoggingEnabled) {
            e.printStackTrace();
        }
        if (fileLoggingEnabled) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(currentLogFile, true))) {
                e.printStackTrace(writer);
                writer.println();
            } catch (IOException ioException) {
                AddonTemplate.LOG.error("Failed to write exception to file: {}", ioException.getMessage());
            }
        }
    }
}
