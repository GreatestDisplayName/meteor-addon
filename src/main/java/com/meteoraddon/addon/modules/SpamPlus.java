package com.meteoraddon.addon.modules;

import com.meteoraddon.addon.AddonTemplate;
import com.meteoraddon.addon.utils.TextDebug;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class SpamPlus extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgMessages = settings.createGroup("messages");
    private final SettingGroup sgTiming = settings.createGroup("timing");

    private final Setting<Boolean> enabled = sgGeneral.add(new BoolSetting.Builder()
        .name("enabled")
        .description("Enable advanced spam functionality.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> randomDelay = sgGeneral.add(new BoolSetting.Builder()
        .name("random-delay")
        .description("Add random delays between messages.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> randomOrder = sgGeneral.add(new BoolSetting.Builder()
        .name("random-order")
        .description("Send messages in random order.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> infiniteLoop = sgGeneral.add(new BoolSetting.Builder()
        .name("infinite-loop")
        .description("Continue spamming infinitely.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> antiSpamBypass = sgGeneral.add(new BoolSetting.Builder()
        .name("anti-spam-bypass")
        .description("Add variations to bypass anti-spam filters.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> disableOnLeave = sgGeneral.add(new BoolSetting.Builder()
        .name("disable-on-leave")
        .description("Automatically disable when leaving server.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> disableOnDisconnect = sgGeneral.add(new BoolSetting.Builder()
        .name("disable-on-disconnect")
        .description("Automatically disable when disconnected from server.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> disableOnLag = sgGeneral.add(new BoolSetting.Builder()
        .name("disable-on-lag")
        .description("Automatically disable when server is lagging.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> lagThreshold = sgGeneral.add(new IntSetting.Builder()
        .name("lag-threshold")
        .description("TPS threshold to detect lag (0-20).")
        .defaultValue(10)
        .min(0)
        .max(20)
        .sliderRange(0, 20)
        .build()
    );

    private final Setting<Integer> loopCount = sgGeneral.add(new IntSetting.Builder()
        .name("loop-count")
        .description("Number of times to loop through messages.")
        .defaultValue(5)
        .min(1)
        .max(100)
        .sliderRange(1, 100)
        .build()
    );

    private final Setting<List<String>> spamMessages = sgMessages.add(new StringListSetting.Builder()
        .name("spam-messages")
        .description("Messages to spam.")
        .defaultValue(List.of(
            "Buy my stuff!",
            "Trading diamonds for emeralds",
            "Free items at spawn!",
            "Join my discord server!",
            "Best shop in the server!"
        ))
        .build()
    );

    private final Setting<String> customMessage = sgMessages.add(new StringSetting.Builder()
        .name("custom-message")
        .description("Custom message to spam (overrides list if not empty).")
        .defaultValue("")
        .build()
    );

    private final Setting<Integer> baseDelay = sgTiming.add(new IntSetting.Builder()
        .name("base-delay")
        .description("Base delay between messages (ticks).")
        .defaultValue(20)
        .min(0)
        .max(200)
        .sliderRange(0, 200)
        .build()
    );

    private final Setting<Integer> randomDelayRange = sgTiming.add(new IntSetting.Builder()
        .name("random-delay-range")
        .description("Additional random delay range (ticks).")
        .defaultValue(10)
        .min(0)
        .max(100)
        .sliderRange(0, 100)
        .build()
    );

    private final Setting<Integer> burstCount = sgTiming.add(new IntSetting.Builder()
        .name("burst-count")
        .description("Number of messages to send in a burst.")
        .defaultValue(1)
        .min(1)
        .max(10)
        .sliderRange(1, 10)
        .build()
    );

    private final Setting<Integer> burstDelay = sgTiming.add(new IntSetting.Builder()
        .name("burst-delay")
        .description("Delay between burst messages (ticks).")
        .defaultValue(2)
        .min(0)
        .max(20)
        .sliderRange(0, 20)
        .build()
    );

    private final Setting<Boolean> customMessageDelays = sgTiming.add(new BoolSetting.Builder()
        .name("custom-message-delays")
        .description("Use custom delays for each message.")
        .defaultValue(false)
        .build()
    );

    private final Setting<List<String>> messageDelays = sgTiming.add(new StringListSetting.Builder()
        .name("message-delays")
        .description("Custom delays per message (format: message:delay in ticks).")
        .defaultValue(List.of(
            "Buy my stuff!:20",
            "Trading diamonds for emeralds:30",
            "Free items at spawn!:15",
            "Join my discord server!:25",
            "Best shop in the server!:10"
        ))
        .build()
    );

    private final Random random = new Random();
    private final AtomicInteger currentLoop = new AtomicInteger(0);
    private final AtomicInteger messageIndex = new AtomicInteger(0);
    private CompletableFuture<Void> spamTask;
    private boolean isSpamming = false;
    private int lagCheckTimer = 0;
    private boolean wasInWorld = false;
    private boolean wasConnected = false;
    private long lastTime = 0;

    public SpamPlus() {
        super(AddonTemplate.CATEGORY, "spam-plus", "Advanced spam functionality with enhanced features.");
    }

    @Override
    public void onActivate() {
        TextDebug.module("SpamPlus", "Activated - Messages: %d, Base delay: %d, Loop count: %d", 
            spamMessages.get().size(), baseDelay.get(), loopCount.get());
        TextDebug.module("SpamPlus", "Anti-spam bypass: %s, Burst count: %d, Lag threshold: %d", 
            antiSpamBypass.get() ? "ON" : "OFF", burstCount.get(), lagThreshold.get());
        
        if (enabled.get()) {
            startSpamming();
        }
    }

    @Override
    public void onDeactivate() {
        stopSpamming();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        // Check for server leave
        if (disableOnLeave.get()) {
            boolean inWorld = mc.world != null && mc.player != null;
            if (wasInWorld && !inWorld) {
                info("Server disconnected, disabling SpamPlus");
                toggle();
                return;
            }
            wasInWorld = inWorld;
        }

        // Check for disconnect (more comprehensive than leave)
        if (disableOnDisconnect.get()) {
            boolean isConnected = mc.getNetworkHandler() != null && mc.player != null;
            if (wasConnected && !isConnected) {
                info("Disconnected from server, disabling SpamPlus");
                toggle();
                return;
            }
            wasConnected = isConnected;
        }

        // Check for lag
        if (disableOnLag.get() && isSpamming) {
            lagCheckTimer++;
            if (lagCheckTimer >= 100) { // Check every 5 seconds (100 ticks)
                lagCheckTimer = 0;
                if (isServerLagging()) {
                    info("Server lag detected, disabling SpamPlus");
                    toggle();
                    return;
                }
            }
        }
    }

    private boolean isServerLagging() {
        if (mc.world == null) return false;
        
        // Simple lag detection based on world time progression
        long currentTime = System.currentTimeMillis();
        
        if (lastTime == 0) {
            lastTime = currentTime;
            return false;
        }
        
        long timeDiff = currentTime - lastTime;
        lastTime = currentTime;
        
        // If tick took longer than expected (200ms for 20 TPS), consider it lag
        return timeDiff > 500; // 500ms threshold for lag detection
    }

    private void startSpamming() {
        if (isSpamming) return;
        
        isSpamming = true;
        currentLoop.set(0);
        messageIndex.set(0);
        
        info("Starting spam+");
        
        spamTask = CompletableFuture.runAsync(() -> {
            while (isSpamming && (infiniteLoop.get() || currentLoop.get() < loopCount.get())) {
                List<String> messages = getMessages();
                
                if (messages.isEmpty()) {
                    error("No messages to spam!");
                    break;
                }
                
                // Send messages
                for (int i = 0; i < burstCount.get() && isSpamming; i++) {
                    String message = getNextMessage(messages);
                    if (message != null && !message.isEmpty()) {
                        sendMessage(message);
                    }
                    
                    if (i < burstCount.get() - 1 && burstDelay.get() > 0) {
                        sleepTicks(burstDelay.get());
                    }
                }
                
                currentLoop.incrementAndGet();
                
                if (isSpamming) {
                    // Get custom delay for the last sent message, or use default
                    String lastMessage = getNextMessage(messages);
                    int delay = getCustomDelay(lastMessage);
                    
                    if (delay > 0) {
                        sleepTicks(delay);
                    }
                }
            }
            
            if (isSpamming) {
                info("Spam+ completed!");
                toggle(); // Turn off module when done
            }
        });
    }

    private void stopSpamming() {
        isSpamming = false;
        if (spamTask != null) {
            spamTask.cancel(true);
            spamTask = null;
        }
        info("Spam+ stopped");
    }

    private List<String> getMessages() {
        if (!customMessage.get().isEmpty()) {
            return List.of(customMessage.get());
        }
        return spamMessages.get();
    }

    private String getNextMessage(List<String> messages) {
        int index;
        if (randomOrder.get()) {
            index = random.nextInt(messages.size());
        } else {
            index = messageIndex.getAndIncrement() % messages.size();
        }
        
        String message = messages.get(index);
        
        if (antiSpamBypass.get()) {
            message = addAntiSpamVariations(message);
        }
        
        return message;
    }

    private String addAntiSpamVariations(String message) {
        // Add random variations to bypass anti-spam
        StringBuilder result = new StringBuilder(message);
        
        // Add random spaces
        if (random.nextBoolean()) {
            int pos = random.nextInt(message.length() / 2);
            result.insert(pos, " ");
        }
        
        // Add random punctuation
        if (random.nextBoolean()) {
            String[] punctuation = {".", "!", "~", "`"};
            result.append(punctuation[random.nextInt(punctuation.length)]);
        }
        
        // Add random numbers (small chance)
        if (random.nextDouble() < 0.1) {
            result.append(" [").append(random.nextInt(999)).append("]");
        }
        
        // Random capitalization
        if (random.nextBoolean()) {
            for (int i = 0; i < result.length(); i++) {
                if (random.nextDouble() < 0.1) {
                    char c = result.charAt(i);
                    result.setCharAt(i, random.nextBoolean() ? 
                        Character.toUpperCase(c) : Character.toLowerCase(c));
                }
            }
        }
        
        return result.toString();
    }

    private int calculateDelay() {
        int delay = baseDelay.get();
        
        if (randomDelay.get()) {
            delay += random.nextInt(randomDelayRange.get() + 1);
        }
        
        return delay;
    }

    private int getCustomDelay(String message) {
        if (!customMessageDelays.get()) return calculateDelay();
        
        for (String delayEntry : messageDelays.get()) {
            String[] parts = delayEntry.split(":", 2);
            if (parts.length == 2) {
                String messagePattern = parts[0].trim();
                try {
                    int customDelay = Integer.parseInt(parts[1].trim());
                    
                    // Check if message matches the pattern
                    if (message.toLowerCase().contains(messagePattern.toLowerCase()) || 
                        messagePattern.equals("*")) {
                        return customDelay;
                    }
                } catch (NumberFormatException e) {
                    // Invalid delay format, skip this entry
                    continue;
                }
            }
        }
        
        // Return default delay if no custom delay found
        return calculateDelay();
    }

    private void sendMessage(String message) {
        if (mc.player != null && mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendChatMessage(message);
        }
    }

    private void sleepTicks(int ticks) {
        try {
            Thread.sleep(ticks * 50L); // 1 tick = 50ms
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void startSpam() {
        if (!isActive()) {
            toggle();
        }
    }

    public void stopSpam() {
        if (isActive()) {
            toggle();
        }
    }

    public boolean isCurrentlySpamming() {
        return isSpamming;
    }

    public int getCurrentLoop() {
        return currentLoop.get();
    }

    public int getProgress() {
        if (infiniteLoop.get()) return -1;
        return (currentLoop.get() * 100) / loopCount.get();
    }
}
