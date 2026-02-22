package com.meteoraddon.addon.modules;

import com.meteoraddon.addon.AddonTemplate;
import com.meteoraddon.addon.utils.TextDebug;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatLogger extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> logIncoming = sgGeneral.add(new BoolSetting.Builder()
        .name("log-incoming")
        .description("Log incoming chat messages.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> logOutgoing = sgGeneral.add(new BoolSetting.Builder()
        .name("log-outgoing")
        .description("Log your outgoing messages.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> logSystem = sgGeneral.add(new BoolSetting.Builder()
        .name("log-system")
        .description("Log system messages (death messages, achievements, etc.).")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> includeTimestamp = sgGeneral.add(new BoolSetting.Builder()
        .name("include-timestamp")
        .description("Include timestamps in log entries.")
        .defaultValue(true)
        .build()
    );

    private final Setting<String> logFileName = sgGeneral.add(new StringSetting.Builder()
        .name("log-file-name")
        .description("Name of the log file.")
        .defaultValue("chat-log")
        .build()
    );

    private final Setting<Boolean> separateFiles = sgGeneral.add(new BoolSetting.Builder()
        .name("separate-files")
        .description("Create separate log files for each day.")
        .defaultValue(true)
        .build()
    );

    private final DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final DateTimeFormatter fileFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ChatLogger() {
        super(AddonTemplate.CATEGORY, "chat-logger", "Logs all chat messages to a file for later review.");
    }

    @Override
    public void onActivate() {
        TextDebug.module("ChatLogger", "Activated - Incoming: %s, Outgoing: %s, System: %s", 
            logIncoming.get() ? "ON" : "OFF", 
            logOutgoing.get() ? "ON" : "OFF", 
            logSystem.get() ? "ON" : "OFF");
        TextDebug.module("ChatLogger", "Log file: %s, Separate files: %s", 
            logFileName.get(), separateFiles.get() ? "YES" : "NO");
    }

    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent event) {
        if (!logIncoming.get()) return;

        String message = event.getMessage().getString();
        String messageType = determineMessageType(message);
        
        if (!shouldLogMessage(messageType)) return;

        TextDebug.trace("ChatLogger", "Received %s message: %s", messageType, message);
        logMessage(message, messageType, false);
    }

    private String determineMessageType(String message) {
        if (message.startsWith("<") || message.startsWith("[") || message.contains(": ")) {
            return "CHAT";
        } else if (message.contains("died") || message.contains("killed") || message.contains("slain")) {
            return "DEATH";
        } else if (message.contains("advancement") || message.contains("achievement")) {
            return "ACHIEVEMENT";
        } else if (message.startsWith("[") && (message.contains("Server") || message.contains("Admin"))) {
            return "SYSTEM";
        } else if (message.contains("joined the game") || message.contains("left the game")) {
            return "JOIN_LEAVE";
        } else {
            return "OTHER";
        }
    }

    private boolean shouldLogMessage(String messageType) {
        switch (messageType) {
            case "CHAT":
                return logIncoming.get();
            case "DEATH":
            case "ACHIEVEMENT":
            case "JOIN_LEAVE":
                return logSystem.get();
            case "SYSTEM":
                return logSystem.get();
            default:
                return logIncoming.get();
        }
    }

    private void logMessage(String message, String messageType, boolean isOutgoing) {
        try {
            Path logDir = Paths.get("meteor-addon", "logs");
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }

            String fileName = logFileName.get();
            if (separateFiles.get()) {
                fileName += "-" + LocalDateTime.now().format(fileFormatter);
            }
            fileName += ".txt";

            Path logFile = logDir.resolve(fileName);
            
            String timestamp = includeTimestamp.get() ? 
                "[" + LocalDateTime.now().format(timestampFormatter) + "] " : "";
            
            String prefix = isOutgoing ? "[OUT] " : "[IN] ";
            String typeTag = "[" + messageType + "] ";
            
            String logEntry = timestamp + prefix + typeTag + message + System.lineSeparator();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile.toFile(), true))) {
                writer.write(logEntry);
            }

        } catch (IOException e) {
            error("Failed to write to chat log: " + e.getMessage());
        }
    }

    public void logOutgoingMessage(String message) {
        if (!logOutgoing.get()) return;
        logMessage(message, "CHAT", true);
    }
}
