package com.meteoraddon.addon.modules;

import com.meteoraddon.addon.AddonTemplate;
import com.meteoraddon.addon.utils.TextDebug;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

public class AutoReply extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgReplies = settings.createGroup("replies");

    private final Setting<Boolean> enabled = sgGeneral.add(new BoolSetting.Builder()
        .name("enabled")
        .description("Enable automatic replies.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Delay between receiving message and sending reply (seconds).")
        .defaultValue(2)
        .min(0)
        .max(30)
        .sliderRange(0, 30)
        .build()
    );

    private final Setting<Boolean> ignoreOwnMessages = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-own-messages")
        .description("Don't reply to your own messages.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> caseSensitive = sgGeneral.add(new BoolSetting.Builder()
        .name("case-sensitive")
        .description("Make keyword matching case sensitive.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> randomReply = sgGeneral.add(new BoolSetting.Builder()
        .name("random-reply")
        .description("Choose random reply from multiple options.")
        .defaultValue(true)
        .build()
    );

    private final Setting<List<String>> keywords = sgReplies.add(new StringListSetting.Builder()
        .name("keywords")
        .description("Keywords to trigger auto-replies (format: keyword:reply1,reply2,reply3).")
        .defaultValue(List.of(
            "hi:hello,hey there,hi!",
            "bye:goodbye,see you later,bye!",
            "thanks:you're welcome,no problem,welcome!",
            "lol:haha,funny,lol!",
            "gg:gg,good game,well played"
        ))
        .build()
    );

    private final Setting<List<String>> playerReplies = sgReplies.add(new StringListSetting.Builder()
        .name("player-replies")
        .description("Player-specific replies (format: playername:reply1,reply2).")
        .defaultValue(List.of())
        .build()
    );

    private final Random random = new Random();
    private final Map<String, Long> lastReplyTime = new HashMap<>();
    private final long COOLDOWN = 5000; // 5 seconds cooldown

    public AutoReply() {
        super(AddonTemplate.CATEGORY, "auto-reply", "Automatically replies to messages.");
    }

    @Override
    public void onActivate() {
        TextDebug.module("AutoReply", "Activated - Delay: %ds, Case sensitive: %s, Random: %s", 
            delay.get(), caseSensitive.get() ? "YES" : "NO", randomReply.get() ? "YES" : "NO");
        TextDebug.module("AutoReply", "Keywords configured: %d, Player replies: %d", 
            keywords.get().size(), playerReplies.get().size());
    }

    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent event) {
        if (!enabled.get()) return;

        String message = event.getMessage().getString();
        String playerName = extractPlayerName(message);
        
        TextDebug.trace("AutoReply", "Processing message: %s from %s", message, playerName);
        
        if (ignoreOwnMessages.get() && isOwnMessage(message, playerName)) {
            TextDebug.trace("AutoReply", "Ignoring own message");
            return;
        }
        
        // Check cooldown
        long currentTime = System.currentTimeMillis();
        if (lastReplyTime.containsKey(playerName) && 
            currentTime - lastReplyTime.get(playerName) < COOLDOWN) {
            return;
        }

        String reply = findReply(message, playerName);
        if (reply != null && !reply.isEmpty()) {
            scheduleReply(reply);
            lastReplyTime.put(playerName, currentTime);
        }
    }

    private String extractPlayerName(String message) {
        // Extract player name from chat message format
        if (message.startsWith("<") && message.contains(">")) {
            int start = message.indexOf("<") + 1;
            int end = message.indexOf(">");
            if (start > 0 && end > start) {
                return message.substring(start, end);
            }
        } else if (message.contains(": ")) {
            int colonIndex = message.indexOf(": ");
            if (colonIndex > 0) {
                return message.substring(0, colonIndex);
            }
        }
        return "unknown";
    }

    private boolean isOwnMessage(String message, String playerName) {
        // Check if message is from current player
        String ownName = mc.player != null ? mc.player.getName().getString() : "";
        return playerName.equalsIgnoreCase(ownName);
    }

    private String findReply(String message, String playerName) {
        String messageContent = extractMessageContent(message);
        
        // Check player-specific replies first
        for (String playerReply : playerReplies.get()) {
            String[] parts = playerReply.split(":", 2);
            if (parts.length == 2 && playerName.equalsIgnoreCase(parts[0].trim())) {
                String[] replies = parts[1].split(",");
                return getRandomReply(replies);
            }
        }

        // Check keyword replies
        for (String keywordReply : keywords.get()) {
            String[] parts = keywordReply.split(":", 2);
            if (parts.length == 2) {
                String keyword = parts[0].trim();
                String[] replies = parts[1].split(",");
                
                if (containsKeyword(messageContent, keyword)) {
                    return getRandomReply(replies);
                }
            }
        }

        return null;
    }

    private String extractMessageContent(String message) {
        // Extract actual message content after player name
        if (message.contains(": ")) {
            int colonIndex = message.indexOf(": ");
            if (colonIndex > 0) {
                return message.substring(colonIndex + 2).trim();
            }
        } else if (message.contains("> ")) {
            int bracketIndex = message.indexOf("> ");
            if (bracketIndex > 0) {
                return message.substring(bracketIndex + 2).trim();
            }
        }
        return message;
    }

    private boolean containsKeyword(String message, String keyword) {
        if (caseSensitive.get()) {
            return message.toLowerCase().contains(keyword.toLowerCase());
        } else {
            return Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE)
                .matcher(message).find();
        }
    }

    private String getRandomReply(String[] replies) {
        if (replies.length == 0) return "";
        
        if (randomReply.get()) {
            return replies[random.nextInt(replies.length)].trim();
        } else {
            return replies[0].trim();
        }
    }

    private void scheduleReply(String reply) {
        if (delay.get() <= 0) {
            sendReply(reply);
        } else {
            // Schedule reply with delay
            new Thread(() -> {
                try {
                    Thread.sleep(delay.get() * 1000L);
                    sendReply(reply);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    private void sendReply(String reply) {
        if (mc.player != null && mc.getNetworkHandler() != null && !reply.isEmpty()) {
            mc.getNetworkHandler().sendChatMessage(reply);
        }
    }
}
