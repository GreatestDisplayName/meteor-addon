package com.meteoraddon.addon.modules;

import com.meteoraddon.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

import java.util.HashMap;
import java.util.Map;

public class SgaTranslator extends Module {
    private final Map<Character, String> sgaMap = new HashMap<>();

    public SgaTranslator() {
        super(AddonTemplate.CATEGORY, "sga-translator", "Translates English messages to Standard Galactic Alphabet.");

        // Initialize SGA mapping
        sgaMap.put('A', "ᔑ");
        sgaMap.put('B', "ʖ");
        sgaMap.put('C', "ᓵ");
        sgaMap.put('D', "↸");
        sgaMap.put('E', "ᒷ");
        sgaMap.put('F', "⎓");
        sgaMap.put('G', "⊣");
        sgaMap.put('H', "⍑");
        sgaMap.put('I', "╎");
        sgaMap.put('J', "リ");
        sgaMap.put('K', "ꖌ");
        sgaMap.put('L', "ꖎ");
        sgaMap.put('M', "ᒲ");
        sgaMap.put('N', "リ");
        sgaMap.put('O', "𝙹");
        sgaMap.put('P', "¡¡");
        sgaMap.put('Q', "↗");
        sgaMap.put('R', "ᓭ");
        sgaMap.put('S', "ᓖ");
        sgaMap.put('T', "ᓴ");
        sgaMap.put('U', "⚍");
        sgaMap.put('V', "⍊");
        sgaMap.put('W', "⌰");
        sgaMap.put('X', "ᔕ");
        sgaMap.put('Y', "⋮");
        sgaMap.put('Z', "ꖌ");
    }

    @EventHandler
    private void onSendMessage(SendMessageEvent event) {
        String message = event.message;
        String sgaMessage = translateToSga(message);
        event.message = sgaMessage;
    }

    private String translateToSga(String text) {
        StringBuilder result = new StringBuilder();
        for (char c : text.toUpperCase().toCharArray()) {
            String sga = sgaMap.get(c);
            if (sga != null) {
                result.append(sga);
            } else {
                result.append(c); // Keep non-letters as is
            }
        }
        return result.toString();
    }
}
