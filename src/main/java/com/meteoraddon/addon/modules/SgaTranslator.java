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

        // EXACT Minecraft Standard Galactic Alphabet Unicode mappings
        // Source: Official Minecraft Wiki and dCode.fr
        sgaMap.put('A', "ᔑ");
        sgaMap.put('B', "ʖ");
        sgaMap.put('C', "ᓵ");
        sgaMap.put('D', "↸");
        sgaMap.put('E', "ᒷ");
        sgaMap.put('F', "⎓");
        sgaMap.put('G', "⊣");
        sgaMap.put('H', "⍑");
        sgaMap.put('I', "╎");
        sgaMap.put('J', "⋮");
        sgaMap.put('K', "ꖌ");
        sgaMap.put('L', "ꖎ");
        sgaMap.put('M', "ᒲ");
        sgaMap.put('N', "リ");
        sgaMap.put('O', "𝙹");
        sgaMap.put('P', "¡¡");  // Note: Two exclamation marks
        sgaMap.put('Q', "ᑑ");
        sgaMap.put('R', "∷");   // Fixed: was ᓭ (wrong)
        sgaMap.put('S', "ᓭ");   // Fixed: was ᓖ (invalid)
        sgaMap.put('T', "ℸ");   // Note: This is "ℸ" (U+2118) with combining character in some fonts, or just "ℸ"
        sgaMap.put('U', "⚍");
        sgaMap.put('V', "⍊");
        sgaMap.put('W', "∴");   // Fixed: was ⌰ (wrong)
        sgaMap.put('X', "̇/");   // Note: Combining dot above + slash (two characters)
        sgaMap.put('Y', "||");  // Fixed: was ⋮ (wrong, that's J)
        sgaMap.put('Z', "⨅");
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