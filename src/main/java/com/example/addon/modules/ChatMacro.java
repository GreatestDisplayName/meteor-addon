package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ChatMacro extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgMacros = settings.createGroup("macros");

    private final Setting<Boolean> enabled = sgGeneral.add(new BoolSetting.Builder()
        .name("enabled")
        .description("Enable chat macros.")
        .defaultValue(true)
        .build()
    );

    private final Setting<String> prefix = sgGeneral.add(new StringSetting.Builder()
        .name("prefix")
        .description("Prefix to trigger macros.")
        .defaultValue(".")
        .build()
    );

    private final Setting<Boolean> caseSensitive = sgGeneral.add(new BoolSetting.Builder()
        .name("case-sensitive")
        .description("Make macro triggers case sensitive.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> showMacroHelp = sgGeneral.add(new BoolSetting.Builder()
        .name("show-macro-help")
        .description("Show available macros when typing prefix + help.")
        .defaultValue(true)
        .build()
    );

    private final Setting<List<String>> macros = sgMacros.add(new StringListSetting.Builder()
        .name("macros")
        .description("Chat macros (format: trigger:message with variables).")
        .defaultValue(List.of(
            "coords:My coordinates are {x}, {y}, {z} in {dimension}!",
            "health:I have {health} health and {hunger} hunger",
            "time:Current time: {time} | Day: {day}",
            "inv:My inventory has {inv_slots} free slots",
            "welcome:Welcome {target} to the server!",
            "trade:Trading {item} for {price} - {target}",
            "help:Available macros: {macro_list}",
            "stats:Kills: {kills} | Deaths: {deaths} | Level: {level}"
        ))
        .build()
    );

    private final Setting<List<String>> customVars = sgMacros.add(new StringListSetting.Builder()
        .name("custom-variables")
        .description("Custom variables (format: name:value).")
        .defaultValue(List.of(
            "server:My Awesome Server",
            "discord:discord.gg/example",
            "website:example.com"
        ))
        .build()
    );

    private final Random random = new Random();
    private final Map<String, String> lastTarget = new HashMap<>();

    public ChatMacro() {
        super(AddonTemplate.CATEGORY, "chat-macro", "Create custom chat macros with variables.");
    }

    @EventHandler
    private void onSendMessage(SendMessageEvent event) {
        if (!enabled.get()) return;

        String message = event.message.trim();
        if (!message.startsWith(prefix.get())) return;

        String trigger = message.substring(prefix.get().length());
        
        // Handle help command
        if (showMacroHelp.get() && trigger.equalsIgnoreCase("help")) {
            event.message = generateMacroHelp();
            return;
        }

        // Find and execute macro
        String macroResult = executeMacro(trigger);
        if (macroResult != null) {
            event.message = macroResult;
        }
    }

    private String executeMacro(String trigger) {
        String[] parts = trigger.split(":", 2);
        if (parts.length != 2) return null;

        String macroName = parts[0].trim();
        String macroContent = parts[1].trim();

        // Find the macro definition
        for (String macro : macros.get()) {
            String[] macroParts = macro.split(":", 2);
            if (macroParts.length == 2) {
                String definedTrigger = macroParts[0].trim();
                String definedContent = macroParts[1].trim();

                if (matchesTrigger(macroName, definedTrigger)) {
                    // Extract target from macro content if specified
                    String target = extractTarget(macroContent);
                    if (target != null) {
                        lastTarget.put(macroName, target);
                    }

                    // Replace variables in the defined content
                    return replaceVariables(definedContent, target);
                }
            }
        }

        return null;
    }

    private boolean matchesTrigger(String input, String defined) {
        if (caseSensitive.get()) {
            return input.equals(defined);
        } else {
            return input.equalsIgnoreCase(defined);
        }
    }

    private String extractTarget(String content) {
        // Look for {target} variable in content to determine if we need to capture a target
        if (content.contains("{target}")) {
            // Try to extract target from the macro call
            String[] parts = content.split(" ", 3);
            if (parts.length >= 2) {
                return parts[1];
            }
        }
        return null;
    }

    private String replaceVariables(String content, String target) {
        String result = content;

        // Player variables
        if (mc.player != null) {
            result = result.replace("{player}", mc.player.getName().getString());
            result = result.replace("{x}", String.valueOf((int) mc.player.getX()));
            result = result.replace("{y}", String.valueOf((int) mc.player.getY()));
            result = result.replace("{z}", String.valueOf((int) mc.player.getZ()));
            result = result.replace("{health}", String.valueOf((int) mc.player.getHealth()));
            result = result.replace("{hunger}", String.valueOf(mc.player.getHungerManager().getFoodLevel()));
            result = result.replace("{xp}", String.valueOf(mc.player.experienceLevel));
            result = result.replace("{dimension}", getDimensionName());
        }

        // World variables
        result = result.replace("{time}", getWorldTime());
        result = result.replace("{day}", String.valueOf(mc.world != null ? mc.world.getTimeOfDay() / 24000L : 0));

        // Inventory variables
        if (mc.player != null) {
            int freeSlots = 0;
            for (int i = 0; i < mc.player.getInventory().size(); i++) {
                if (mc.player.getInventory().getStack(i).isEmpty()) {
                    freeSlots++;
                }
            }
            result = result.replace("{inv_slots}", String.valueOf(freeSlots));
        }

        // Target variable
        if (target != null) {
            result = result.replace("{target}", target);
        } else if (lastTarget.containsKey(content.split(":")[0])) {
            result = result.replace("{target}", lastTarget.get(content.split(":")[0]));
        }

        // Custom variables
        for (String customVar : customVars.get()) {
            String[] varParts = customVar.split(":", 2);
            if (varParts.length == 2) {
                String varName = varParts[0].trim();
                String varValue = varParts[1].trim();
                result = result.replace("{" + varName + "}", varValue);
            }
        }

        // Special variables
        result = result.replace("{macro_list}", getMacroList());
        result = result.replace("{random}", String.valueOf(random.nextInt(100)));
        result = result.replace("{time_real}", java.time.LocalTime.now().toString());

        // Placeholder stats (would need integration with stat tracking)
        result = result.replace("{kills}", "0");
        result = result.replace("{deaths}", "0");
        result = result.replace("{level}", String.valueOf(mc.player != null ? mc.player.experienceLevel : 0));

        return result;
    }

    private String getDimensionName() {
        if (mc.world == null) return "Unknown";
        
        String dimension = mc.world.getRegistryKey().getValue().toString();
        switch (dimension) {
            case "minecraft:overworld": return "Overworld";
            case "minecraft:nether": return "Nether";
            case "minecraft:the_end": return "The End";
            default: return dimension;
        }
    }

    private String getWorldTime() {
        if (mc.world == null) return "0:00";
        
        long time = mc.world.getTimeOfDay() % 24000L;
        int hours = (int) (time / 1000L) + 6; // Minecraft time starts at 6 AM
        int minutes = (int) ((time % 1000L) * 60 / 1000);
        
        if (hours >= 24) hours -= 24;
        
        return String.format("%02d:%02d", hours, minutes);
    }

    private String getMacroList() {
        StringBuilder list = new StringBuilder();
        for (String macro : macros.get()) {
            String[] parts = macro.split(":", 2);
            if (parts.length == 2) {
                if (list.length() > 0) list.append(", ");
                list.append(parts[0].trim());
            }
        }
        return list.toString();
    }

    private String generateMacroHelp() {
        StringBuilder help = new StringBuilder();
        help.append("Available macros: ");
        
        for (String macro : macros.get()) {
            String[] parts = macro.split(":", 2);
            if (parts.length == 2) {
                help.append("\n").append(prefix.get()).append(parts[0].trim());
                help.append(" - ").append(parts[1].trim().substring(0, Math.min(30, parts[1].trim().length())));
                if (parts[1].trim().length() > 30) help.append("...");
            }
        }
        
        return help.toString();
    }
}
