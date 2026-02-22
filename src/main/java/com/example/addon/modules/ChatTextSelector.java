package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.Text;

public class ChatTextSelector extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> clickableMessages = sgGeneral.add(new BoolSetting.Builder()
        .name("clickable-messages")
        .description("Make chat messages clickable to copy their content.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> overrideClickEvents = sgGeneral.add(new BoolSetting.Builder()
        .name("override-click-events")
        .description("Override existing click events on messages (e.g., server links).")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> antiSpam = sgGeneral.add(new BoolSetting.Builder()
        .name("anti-spam")
        .description("Enable anti-spam to filter duplicate messages.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> spamThreshold = sgGeneral.add(new IntSetting.Builder()
        .name("spam-threshold")
        .description("Number of identical messages before filtering (1-10).")
        .defaultValue(3)
        .min(1)
        .max(10)
        .sliderRange(1, 10)
        .build()
    );

    private final Setting<Integer> spamTimeWindow = sgGeneral.add(new IntSetting.Builder()
        .name("spam-time-window")
        .description("Time window in seconds to detect spam (5-60).")
        .defaultValue(10)
        .min(5)
        .max(60)
        .sliderRange(5, 60)
        .build()
    );

    private final Setting<Boolean> exploitProtection = sgGeneral.add(new BoolSetting.Builder()
        .name("exploit-protection")
        .description("Enable exploit protection to prevent abuse.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> maxCopiesPerMinute = sgGeneral.add(new IntSetting.Builder()
        .name("max-copies-per-minute")
        .description("Maximum clipboard operations per minute (5-60).")
        .defaultValue(20)
        .min(5)
        .max(60)
        .sliderRange(5, 60)
        .build()
    );

    private final Setting<Integer> maxMessageLength = sgGeneral.add(new IntSetting.Builder()
        .name("max-message-length")
        .description("Maximum message length to store (100-1000).")
        .defaultValue(500)
        .min(100)
        .max(1000)
        .sliderRange(100, 1000)
        .build()
    );

    private String lastMessage = "";
    private java.util.List<String> messageHistory = new java.util.ArrayList<>();
    private final int maxHistory = 10; // Store last 10 messages
    
    // Anti-spam tracking
    private java.util.Map<String, java.util.List<Long>> messageTimestamps = new java.util.HashMap<>();
    private java.util.Set<String> filteredMessages = new java.util.HashSet<>();
    
    // Exploit protection tracking
    private java.util.List<Long> copyTimestamps = new java.util.ArrayList<>();
    private long lastExploitWarning = 0;

    public ChatTextSelector() {
        super(AddonTemplate.CATEGORY, "chat-text-selector", "Makes chat messages clickable to copy text content.");
    }

    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent event) {
        if (!isActive() || !clickableMessages.get()) return;

        Text message = event.getMessage();
        if (message == null) return;

        // Skip if message already has a click event and we shouldn't override it
        if (!overrideClickEvents.get() && message.getStyle().getClickEvent() != null) {
            return;
        }

        String messageText = message.getString();
        
        // ✅ Exploit protection: Message length validation
        if (exploitProtection.get() && messageText.length() > maxMessageLength.get()) {
            // Truncate overly long messages
            messageText = messageText.substring(0, maxMessageLength.get()) + "... [TRUNCATED]";
        }
        
        // ✅ Anti-spam check
        if (antiSpam.get() && isSpam(messageText)) {
            // Filter out spam messages
            event.setMessage(null);
            return;
        }
        
        // ✅ Store message in history
        lastMessage = messageText;
        
        // Add to history (keep only last maxHistory messages)
        messageHistory.add(0, messageText); // Add to beginning (newest first)
        if (messageHistory.size() > maxHistory) {
            messageHistory.remove(messageHistory.size() - 1); // Remove oldest
        }
        
        // Create a simple message with copy instruction
        Text modifiedMessage = Text.literal(messageText + " §7[Copy with .chatcopy or .chatlist]")
            .styled(style -> style.withColor(net.minecraft.util.Formatting.WHITE));
        
        event.setMessage(modifiedMessage);
    }

    private boolean isSpam(String message) {
        if (!antiSpam.get()) return false;
        
        long currentTime = System.currentTimeMillis();
        long timeWindowMs = spamTimeWindow.get() * 1000L; // Convert seconds to milliseconds
        
        // Get or create timestamp list for this message
        java.util.List<Long> timestamps = messageTimestamps.computeIfAbsent(message, k -> new java.util.ArrayList<>());
        
        // Clean old timestamps outside the time window
        timestamps.removeIf(time -> currentTime - time > timeWindowMs);
        
        // Add current timestamp
        timestamps.add(currentTime);
        
        // Check if this message exceeds the spam threshold
        if (timestamps.size() >= spamThreshold.get()) {
            filteredMessages.add(message);
            return true;
        }
        
        return false;
    }

    private boolean checkCopyRateLimit() {
        if (!exploitProtection.get()) return true;
        
        long currentTime = System.currentTimeMillis();
        long oneMinuteAgo = currentTime - 60000; // 60 seconds ago
        
        // Clean old timestamps outside the last minute
        copyTimestamps.removeIf(time -> time < oneMinuteAgo);
        
        // Check if rate limit exceeded
        if (copyTimestamps.size() >= maxCopiesPerMinute.get()) {
            // Show warning only once per minute
            if (currentTime - lastExploitWarning > 60000) {
                error("Copy rate limit exceeded! Max %d copies per minute.", maxCopiesPerMinute.get());
                lastExploitWarning = currentTime;
            }
            return false;
        }
        
        // Add current timestamp
        copyTimestamps.add(currentTime);
        return true;
    }

    private boolean validateMessageForCopy(String message) {
        if (!exploitProtection.get()) return true;
        
        // Check for suspicious content
        if (message == null || message.trim().isEmpty()) {
            error("Cannot copy empty message!");
            return false;
        }
        
        // Check for potential exploit patterns
        String lowerMessage = message.toLowerCase();
        if (lowerMessage.contains("@everyone") || lowerMessage.contains("@here") || 
            lowerMessage.contains("discord.gg/") || lowerMessage.contains("http://") ||
            lowerMessage.contains("https://") && message.length() > 200) {
            error("Message contains potentially suspicious content!");
            return false;
        }
        
        return true;
    }

    public void clearSpamData() {
        messageTimestamps.clear();
        filteredMessages.clear();
        copyTimestamps.clear();
        lastExploitWarning = 0;
        info("Anti-spam and exploit protection data cleared.");
    }

    public void showSpamStats() {
        int totalTracked = messageTimestamps.size();
        int totalFiltered = filteredMessages.size();
        int recentCopies = copyTimestamps.size();
        info("Anti-spam stats: %d unique messages tracked, %d filtered as spam.", totalTracked, totalFiltered);
        info("Exploit protection: %d copies in last minute (max: %d).", recentCopies, maxCopiesPerMinute.get());
    }

    public void showExploitStatus() {
        info("=== Exploit Protection Status ===");
        info("Protection enabled: %s", exploitProtection.get() ? "Yes" : "No");
        info("Max copies per minute: %d", maxCopiesPerMinute.get());
        info("Max message length: %d", maxMessageLength.get());
        info("Copies in last minute: %d", copyTimestamps.size());
        info("Rate limit status: %s", copyTimestamps.size() >= maxCopiesPerMinute.get() ? "LIMITED" : "OK");
    }

    public void copyLastMessage() {
        if (!checkCopyRateLimit()) return;
        
        if (lastMessage != null && !lastMessage.isEmpty()) {
            if (!validateMessageForCopy(lastMessage)) return;
            
            mc.keyboard.setClipboard(lastMessage);
            info("Copied to clipboard: \"%s\"", lastMessage.length() > 50 ? lastMessage.substring(0, 47) + "..." : lastMessage);
        } else {
            error("No message to copy!");
        }
    }

    public void copyMessage(int index) {
        if (!checkCopyRateLimit()) return;
        
        if (index >= 0 && index < messageHistory.size()) {
            String message = messageHistory.get(index);
            if (!validateMessageForCopy(message)) return;
            
            mc.keyboard.setClipboard(message);
            info("Copied message %d to clipboard: \"%s\"", index + 1, message.length() > 50 ? message.substring(0, 47) + "..." : message);
        } else {
            error("Invalid message index! Use .chatlist to see available messages.");
        }
    }

    public void listMessages() {
        if (messageHistory.isEmpty()) {
            info("No messages in history.");
            return;
        }

        info("=== Chat History (newest first) ===");
        for (int i = 0; i < messageHistory.size(); i++) {
            String message = messageHistory.get(i);
            // Truncate long messages for display
            String display = message.length() > 50 ? message.substring(0, 47) + "..." : message;
            info("%d. %s", i + 1, display);
        }
        info("Use .chatcopy <number> to copy specific message (e.g., .chatcopy 1)");
    }
}