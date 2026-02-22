package com.meteoraddon.addon.modules;

import com.meteoraddon.addon.AddonTemplate;
import com.meteoraddon.addon.utils.TextDebug;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.regex.Pattern;

public class MessageHighlighter extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgColors = settings.createGroup("colors");

    private final Setting<Boolean> enabled = sgGeneral.add(new BoolSetting.Builder()
        .name("enabled")
        .description("Enable message highlighting.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> highlightKeywords = sgGeneral.add(new BoolSetting.Builder()
        .name("highlight-keywords")
        .description("Highlight messages containing specific keywords.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> highlightPlayers = sgGeneral.add(new BoolSetting.Builder()
        .name("highlight-players")
        .description("Highlight messages from specific players.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> highlightOwnName = sgGeneral.add(new BoolSetting.Builder()
        .name("highlight-own-name")
        .description("Highlight messages containing your own name.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> caseSensitive = sgGeneral.add(new BoolSetting.Builder()
        .name("case-sensitive")
        .description("Make keyword matching case sensitive.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> wholeWord = sgGeneral.add(new BoolSetting.Builder()
        .name("whole-word")
        .description("Match whole words only.")
        .defaultValue(true)
        .build()
    );

    private final Setting<List<String>> keywords = sgGeneral.add(new StringListSetting.Builder()
        .name("keywords")
        .description("Keywords to highlight in messages.")
        .defaultValue(List.of(
            "admin", "mod", "staff", "help", "important", "warning", "alert",
            "trade", "buy", "sell", "price", "deal", "offer"
        ))
        .build()
    );

    private final Setting<List<String>> players = sgGeneral.add(new StringListSetting.Builder()
        .name("players")
        .description("Player names to highlight.")
        .defaultValue(List.of())
        .build()
    );

    private final ColorSetting keywordColor = sgColors.add(new ColorSetting.Builder()
        .name("keyword-color")
        .description("Color for highlighted keywords.")
        .defaultValue(new meteordevelopment.meteorclient.utils.render.color.Color(255, 255, 0)) // Yellow
        .build()
    );

    private final ColorSetting playerColor = sgColors.add(new ColorSetting.Builder()
        .name("player-color")
        .description("Color for highlighted player messages.")
        .defaultValue(new meteordevelopment.meteorclient.utils.render.color.Color(0, 255, 255)) // Cyan
        .build()
    );

    private final ColorSetting ownNameColor = sgColors.add(new ColorSetting.Builder()
        .name("own-name-color")
        .description("Color for messages containing your name.")
        .defaultValue(new meteordevelopment.meteorclient.utils.render.color.Color(255, 0, 255)) // Magenta
        .build()
    );

    private final Setting<Boolean> boldHighlight = sgColors.add(new BoolSetting.Builder()
        .name("bold-highlight")
        .description("Make highlighted text bold.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> underlineHighlight = sgColors.add(new BoolSetting.Builder()
        .name("underline-highlight")
        .description("Underline highlighted text.")
        .defaultValue(false)
        .build()
    );

    public MessageHighlighter() {
        super(AddonTemplate.CATEGORY, "message-highlighter", "Highlight messages containing specific keywords or player names.");
    }

    @Override
    public void onActivate() {
        TextDebug.module("MessageHighlighter", "Activated - Keywords: %d, Players: %d", 
            keywords.get().size(), players.get().size());
        TextDebug.module("MessageHighlighter", "Case sensitive: %s, Whole word: %s, Bold: %s", 
            caseSensitive.get() ? "YES" : "NO", wholeWord.get() ? "YES" : "NO", boldHighlight.get() ? "YES" : "NO");
    }

    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent event) {
        if (!enabled.get()) return;

        Text originalMessage = event.getMessage();
        String messageText = originalMessage.getString();
        String playerName = extractPlayerName(messageText);
        
        TextDebug.trace("MessageHighlighter", "Processing message: %s from %s", messageText, playerName);
        
        boolean shouldHighlight = false;
        Color highlightColor = null;
        String messageContent = extractMessageContent(messageText);

        // Check for own name
        if (highlightOwnName.get() && containsOwnName(messageContent)) {
            shouldHighlight = true;
            highlightColor = ownNameColor.get();
        }

        // Check for player highlights
        if (highlightPlayers.get() && isHighlightedPlayer(playerName)) {
            shouldHighlight = true;
            highlightColor = playerColor.get();
        }

        // Check for keyword highlights
        if (highlightKeywords.get() && containsKeywords(messageContent)) {
            shouldHighlight = true;
            if (highlightColor == null) {
                highlightColor = keywordColor.get();
            }
        }

        if (shouldHighlight && highlightColor != null) {
            Text highlightedMessage = createHighlightedText(originalMessage, highlightColor);
            event.setMessage(highlightedMessage);
        }
    }

    private String extractPlayerName(String message) {
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
        return "";
    }

    private String extractMessageContent(String message) {
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

    private boolean containsOwnName(String message) {
        if (mc.player == null) return false;
        String ownName = mc.player.getName().getString();
        return containsKeyword(message, ownName);
    }

    private boolean isHighlightedPlayer(String playerName) {
        if (playerName.isEmpty()) return false;
        
        for (String highlightedPlayer : players.get()) {
            if (playerName.equalsIgnoreCase(highlightedPlayer.trim())) {
                return true;
            }
        }
        return false;
    }

    private boolean containsKeywords(String message) {
        for (String keyword : keywords.get()) {
            if (containsKeyword(message, keyword.trim())) {
                return true;
            }
        }
        return false;
    }

    private boolean containsKeyword(String message, String keyword) {
        if (keyword.isEmpty()) return false;

        String searchMessage = caseSensitive.get() ? message : message.toLowerCase();
        String searchKeyword = caseSensitive.get() ? keyword : keyword.toLowerCase();

        if (wholeWord.get()) {
            return Pattern.compile("\\b" + Pattern.quote(searchKeyword) + "\\b", 
                caseSensitive.get() ? 0 : Pattern.CASE_INSENSITIVE)
                .matcher(searchMessage).find();
        } else {
            return searchMessage.contains(searchKeyword);
        }
    }

    private Text createHighlightedText(Text originalText, meteordevelopment.meteorclient.utils.render.color.Color color) {
        MutableText highlightedText = Text.literal(originalText.getString());
        
        Formatting formatting = getFormattingFromColor(color);
        highlightedText.formatted(formatting);
        
        if (boldHighlight.get()) {
            highlightedText.formatted(Formatting.BOLD);
        }
        
        if (underlineHighlight.get()) {
            highlightedText.formatted(Formatting.UNDERLINE);
        }

        return highlightedText;
    }

    private Formatting getFormattingFromColor(meteordevelopment.meteorclient.utils.render.color.Color color) {
        // Convert Color to nearest Minecraft formatting color
        float[] hsb = java.awt.Color.RGBtoHSB(color.r, color.g, color.b, null);
        float hue = hsb[0];
        float saturation = hsb[1];
        float brightness = hsb[2];

        if (brightness < 0.1) return Formatting.BLACK;
        if (saturation < 0.1) {
            if (brightness < 0.3) return Formatting.DARK_GRAY;
            if (brightness < 0.7) return Formatting.GRAY;
            return Formatting.WHITE;
        }

        // Color based on hue
        if (hue < 0.083) return Formatting.RED;
        if (hue < 0.167) return Formatting.GOLD;
        if (hue < 0.25) return Formatting.YELLOW;
        if (hue < 0.333) return Formatting.GREEN;
        if (hue < 0.417) return Formatting.DARK_GREEN;
        if (hue < 0.5) return Formatting.DARK_AQUA;
        if (hue < 0.583) return Formatting.AQUA;
        if (hue < 0.667) return Formatting.BLUE;
        if (hue < 0.75) return Formatting.DARK_BLUE;
        if (hue < 0.833) return Formatting.LIGHT_PURPLE;
        if (hue < 0.917) return Formatting.DARK_PURPLE;
        return Formatting.RED;
    }
}
