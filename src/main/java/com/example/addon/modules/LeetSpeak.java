package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

import java.util.HashMap;
import java.util.Map;

public class LeetSpeak extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> enabled = sgGeneral.add(new BoolSetting.Builder()
        .name("enabled")
        .description("Enable leetspeak conversion in chat messages.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> randomCase = sgGeneral.add(new BoolSetting.Builder()
        .name("random-case")
        .description("Randomly change letter case for more leet effect.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> advancedLeet = sgGeneral.add(new BoolSetting.Builder()
        .name("advanced-leet")
        .description("Use advanced leetspeak substitutions.")
        .defaultValue(false)
        .build()
    );

    private final Map<Character, String> basicLeetMap = new HashMap<>();
    private final Map<Character, String> advancedLeetMap = new HashMap<>();

    public LeetSpeak() {
        super(AddonTemplate.CATEGORY, "leet-speak", "Converts your chat messages to leetspeak.");

        // Basic leetspeak mappings
        basicLeetMap.put('a', "4");
        basicLeetMap.put('e', "3");
        basicLeetMap.put('i', "1");
        basicLeetMap.put('o', "0");
        basicLeetMap.put('s', "5");
        basicLeetMap.put('t', "7");
        basicLeetMap.put('l', "1");
        basicLeetMap.put('g', "6");
        basicLeetMap.put('b', "8");

        // Advanced leetspeak mappings
        advancedLeetMap.put('a', "@");
        advancedLeetMap.put('b', "|3");
        advancedLeetMap.put('c', "(");
        advancedLeetMap.put('d', "|)");
        advancedLeetMap.put('e', "3");
        advancedLeetMap.put('f', "ph");
        advancedLeetMap.put('g', "9");
        advancedLeetMap.put('h', "#");
        advancedLeetMap.put('i', "1");
        advancedLeetMap.put('j', "_|");
        advancedLeetMap.put('k', "|<");
        advancedLeetMap.put('l', "1");
        advancedLeetMap.put('m', "/\\/\\");
        advancedLeetMap.put('n', "|\\|");
        advancedLeetMap.put('o', "0");
        advancedLeetMap.put('p', "|D");
        advancedLeetMap.put('q', "(,)");
        advancedLeetMap.put('r', "|2");
        advancedLeetMap.put('s', "5");
        advancedLeetMap.put('t', "7");
        advancedLeetMap.put('u', "|_|");
        advancedLeetMap.put('v', "\\/");
        advancedLeetMap.put('w', "\\/\\/");
        advancedLeetMap.put('x', "><");
        advancedLeetMap.put('y', "`/");
        advancedLeetMap.put('z', "2");
    }

    @EventHandler
    private void onSendMessage(SendMessageEvent event) {
        if (!enabled.get()) return;

        String message = event.message;
        String converted = convertToLeet(message);
        
        if (!message.equals(converted)) {
            event.message = converted;
        }
    }

    private String convertToLeet(String input) {
        StringBuilder result = new StringBuilder();
        Map<Character, String> leetMap = advancedLeet.get() ? advancedLeetMap : basicLeetMap;

        for (char c : input.toCharArray()) {
            char lowerC = Character.toLowerCase(c);
            
            if (leetMap.containsKey(lowerC)) {
                String replacement = leetMap.get(lowerC);
                
                if (randomCase.get()) {
                    replacement = randomizeCase(replacement);
                }
                
                result.append(replacement);
            } else {
                if (randomCase.get() && Math.random() > 0.7) {
                    result.append(Character.isUpperCase(c) ? Character.toLowerCase(c) : Character.toUpperCase(c));
                } else {
                    result.append(c);
                }
            }
        }

        return result.toString();
    }

    private String randomizeCase(String input) {
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Math.random() > 0.5) {
                result.append(Character.toUpperCase(c));
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }
}
