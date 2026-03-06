package com.meteoraddon.addon.modules;

import com.meteoraddon.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class EncryptMessage extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> key = sgGeneral.add(new StringSetting.Builder()
        .name("key")
        .description("AES encryption key.")
        .defaultValue("defaultkey123456")
        .build()
    );

    public EncryptMessage() {
        super(AddonTemplate.CATEGORY, "encrypt-message", "Encrypts chat messages using AES encryption.");
    }

    @EventHandler
    private void onSendMessage(SendMessageEvent event) {
        String message = event.message;
        String encryptedMessage = encrypt(message, key.get());
        event.message = encryptedMessage;
    }

    private String encrypt(String text, String keyStr) {
        try {
            SecretKey secretKey = getKey(keyStr);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(text.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            e.printStackTrace();
            return text; // fallback
        }
    }

    private SecretKey getKey(String key) {
        byte[] keyBytes = key.getBytes();
        byte[] padded = new byte[16];
        System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 16));
        return new SecretKeySpec(padded, "AES");
    }
}
