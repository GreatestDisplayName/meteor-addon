package com.meteoraddon.addon.modules;

import com.meteoraddon.addon.AddonTemplate;

import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class KaboomChatFiller extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> filterNBT = sgGeneral.add(new BoolSetting.Builder()
        .name("filter-nbt-spam")
        .description("Blocks messages containing NBT data spam.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> filterFormatExploit = sgGeneral.add(new BoolSetting.Builder()
        .name("filter-format-exploit")
        .description("Blocks messages with %1$s format string exploits.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> filterJapaneseSpam = sgGeneral.add(new BoolSetting.Builder()
        .name("filter-japanese-spam")
        .description("Blocks messages with excessive Japanese character spam (あ).")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> japaneseThreshold = sgGeneral.add(new IntSetting.Builder()
        .name("japanese-threshold")
        .description("Number of repeated Japanese chars before blocking.")
        .defaultValue(10)
        .min(5)
        .max(100)
        .visible(filterJapaneseSpam::get)
        .build()
    );

    private final Setting<Boolean> filterErrorMessages = sgGeneral.add(new BoolSetting.Builder()
        .name("filter-error-messages")
        .description("Blocks 'Can't deliver chat message' error spam.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> verbose = sgGeneral.add(new BoolSetting.Builder()
        .name("verbose")
        .description("Show info when messages are blocked.")
        .defaultValue(false)
        .build()
    );

    public KaboomChatFiller() {
        super(AddonTemplate.CATEGORY, "kaboom-chat-filler", "Filters out NBT and format string spam from kaboom.pw");
    }

    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent event) {
        String message = event.getMessage().getString();
        
        // Filter NBT data spam
        if (filterNBT.get() && containsNBTSpam(message)) {
            event.cancel();
            if (verbose.get()) info("Blocked NBT spam");
            return;
        }

        // Filter format string exploits
        if (filterFormatExploit.get() && containsFormatExploit(message)) {
            event.cancel();
            if (verbose.get()) info("Blocked format exploit");
            return;
        }

        // Filter Japanese character spam
        if (filterJapaneseSpam.get() && containsJapaneseSpam(message)) {
            event.cancel();
            if (verbose.get()) info("Blocked Japanese spam");
            return;
        }

        // Filter error messages
        if (filterErrorMessages.get() && message.contains("Can't deliver chat message")) {
            event.cancel();
            if (verbose.get()) info("Blocked error message");
            return;
        }
    }

    private boolean containsNBTSpam(String message) {
        // Check for NBT-like patterns
        return message.contains("AbsorptionAmount:") || 
               message.contains("Bukkit.updateLevel:") ||
               message.contains("DataVersion:") ||
               message.contains("UUID:[I;") ||
               message.contains("playerGameType:") ||
               (message.contains("{") && message.contains(":") && message.length() > 500);
    }

    private boolean containsFormatExploit(String message) {
        // Check for excessive format specifiers
        int count = 0;
        int index = 0;
        while ((index = message.indexOf("%1$s", index)) != -1) {
            count++;
            index += 4;
            if (count > 5) return true; // More than 5 format specifiers is suspicious
        }
        return false;
    }

    private boolean containsJapaneseSpam(String message) {
        // Count repeated あ characters
        int count = 0;
        int maxRepeated = 0;
        char prevChar = '\0';
        
        for (char c : message.toCharArray()) {
            if (c == 'あ') {
                if (c == prevChar) {
                    count++;
                    maxRepeated = Math.max(maxRepeated, count);
                } else {
                    count = 1;
                }
                prevChar = c;
            } else {
                count = 0;
                prevChar = '\0';
            }
        }
        
        // Also check total count of あ in message
        long totalCount = message.chars().filter(ch -> ch == 'あ').count();
        
        return maxRepeated >= japaneseThreshold.get() || totalCount >= japaneseThreshold.get();
    }
}
