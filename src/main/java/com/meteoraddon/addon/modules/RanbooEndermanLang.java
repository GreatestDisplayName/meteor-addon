package com.meteoraddon.addon.modules;

import com.meteoraddon.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

import java.util.HashMap;
import java.util.Map;

public class RanbooEndermanLang extends Module {
    private final Map<Character, String> endermanMap = new HashMap<>();

    public RanbooEndermanLang() {
        super(AddonTemplate.CATEGORY, "ranboo-enderman-lang", "Translates English messages to Ranboo Enderman language.");

        // Initialize Enderman language mapping
        endermanMap.put('A', "~");
        endermanMap.put('B', "§");
        endermanMap.put('C', "†");
        endermanMap.put('D', "‡");
        endermanMap.put('E', "¥");
        endermanMap.put('F', "µ");
        endermanMap.put('G', "¶");
        endermanMap.put('H', "•");
        endermanMap.put('I', "°");
        endermanMap.put('J', "±");
        endermanMap.put('K', "²");
        endermanMap.put('L', "³");
        endermanMap.put('M', "´");
        endermanMap.put('N', "¹");
        endermanMap.put('O', "º");
        endermanMap.put('P', "»");
        endermanMap.put('Q', "¼");
        endermanMap.put('R', "½");
        endermanMap.put('S', "¾");
        endermanMap.put('T', "¿");
        endermanMap.put('U', "×");
        endermanMap.put('V', "÷");
        endermanMap.put('W', "¤");
        endermanMap.put('X', "¦");
        endermanMap.put('Y', "¨");
        endermanMap.put('Z', "©");
    }

    @EventHandler
    private void onSendMessage(SendMessageEvent event) {
        String message = event.message;
        String endermanMessage = translateToEnderman(message);
        event.message = endermanMessage;
    }

    private String translateToEnderman(String text) {
        StringBuilder result = new StringBuilder();
        for (char c : text.toUpperCase().toCharArray()) {
            String enderman = endermanMap.get(c);
            if (enderman != null) {
                result.append(enderman);
            } else {
                result.append(c); // Keep non-letters as is
            }
        }
        return result.toString();
    }
}
