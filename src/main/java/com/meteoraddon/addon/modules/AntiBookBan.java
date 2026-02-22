package com.meteoraddon.addon.modules;

import com.meteoraddon.addon.AddonTemplate;
import com.meteoraddon.addon.utils.TextDebug;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;
import java.util.regex.Pattern;

public class AntiBookBan extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<Boolean> banBooks = sgGeneral.add(new BoolSetting.Builder()
        .name("ban-books")
        .description("Ban players who have books in inventory.")
        .defaultValue(true)
        .build()
    );
    
    private final Setting<Boolean> showMessage = sgGeneral.add(new BoolSetting.Builder()
        .name("show-message")
        .description("Show message when book is detected and banned.")
        .defaultValue(true)
        .build()
    );
    
    private final Setting<Boolean> kickPlayer = sgGeneral.add(new BoolSetting.Builder()
        .name("kick-player")
        .description("Kick player from world when banned book detected.")
        .defaultValue(false)
        .build()
    );
    
    private final Setting<List<String>> allowedPlayers = sgGeneral.add(new StringListSetting.Builder()
        .name("allowed-players")
        .description("Players who are exempt from book ban.")
        .defaultValue(List.of())
        .build()
    );
    
    // Patterns to detect book-related items
    private static final Pattern BOOK_PATTERN = Pattern.compile(
        "writable_book|written_book|enchanted_book", 
        Pattern.CASE_INSENSITIVE
    );
    
    private int detectionCount = 0;
    private boolean lastHadBooks = false;

    public AntiBookBan() {
        super(AddonTemplate.CATEGORY, "anti-book-ban", "Detects and bans players with books in inventory.");
    }

    @Override
    public void onActivate() {
        TextDebug.module("AntiBookBan", "Activated - Ban: %s, Kick: %s, Show: %s", 
            banBooks.get() ? "YES" : "NO",
            kickPlayer.get() ? "YES" : "NO",
            showMessage.get() ? "YES" : "NO");
        TextDebug.module("AntiBookBan", "Allowed players: %d", allowedPlayers.get().size());
        detectionCount = 0;
        lastHadBooks = false;
    }

    @Override
    public void onDeactivate() {
        TextDebug.module("AntiBookBan", "Deactivated - Total detections: %d", detectionCount);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (mc.player == null) return;
        
        // Check if current player is in allowed list
        String playerName = mc.player.getName().getString();
        if (allowedPlayers.get().contains(playerName)) {
            return;
        }
        
        // Check if player has books
        boolean hasBooks = hasBooksInInventory();
        
        // Detect when player gets books
        if (hasBooks && !lastHadBooks) {
            handleBookDetection(playerName);
            lastHadBooks = true;
        } else if (!hasBooks && lastHadBooks) {
            lastHadBooks = false;
        }
    }

    private boolean hasBooksInInventory() {
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack != null && isBannedBook(stack)) {
                return true;
            }
        }
        return false;
    }

    private boolean isBannedBook(ItemStack stack) {
        if (stack == null) return false;
        
        String itemName = stack.getItem().toString();
        return BOOK_PATTERN.matcher(itemName).find();
    }

    private void handleBookDetection(String playerName) {
        detectionCount++;
        
        // Log detection
        if (showMessage.get()) {
            TextDebug.module("AntiBookBan", "Book detected on player: %s", playerName);
        }
        
        // Show message to player
        if (showMessage.get()) {
            Text warning = Text.literal("[AntiBookBan] Books are not allowed! Remove them from your inventory.");
            
            mc.player.sendMessage(warning, true);
        }
        
        // Kick player if enabled
        if (kickPlayer.get()) {
            Text kickMessage = Text.literal("Kicked " + playerName + " for having books in inventory.");
            
            mc.player.networkHandler.sendChatMessage(kickMessage.getString());
        }
    }
}
