package com.meteoraddon.addon.modules;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;

import com.meteoraddon.addon.AddonTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.lang.reflect.Field;

public class SignPlus extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> dateFormat = sgGeneral.add(new StringSetting.Builder()
        .name("date-format")
        .description("Date format pattern (e.g., dd/MMM/yy, MM/dd/yy)")
        .defaultValue("MM/dd/yy")
        .build()
    );

    private final Setting<String> shorthand = sgGeneral.add(new StringSetting.Builder()
        .name("shorthand")
        .description("Shorthand text to replace with date (e.g., date, %date%)")
        .defaultValue("date")
        .build()
    );

    public SignPlus() {
        super(AddonTemplate.CATEGORY, "sign-plus", "Replaces shorthand text with current date in signs.");
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (!(event.packet instanceof UpdateSignC2SPacket packet)) return;

        try {
            // 1. Get the original lines from the packet
            String[] lines = packet.getText();
            boolean modified = false;
            String shorthandText = shorthand.get();
            String date;

            // 2. Format the date
            try {
                date = LocalDate.now().format(DateTimeFormatter.ofPattern(dateFormat.get()));
            } catch (Exception e) {
                error("Invalid date format: " + dateFormat.get());
                return;
            }

            // 3. Replace the shorthand in the lines array
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].contains(shorthandText)) {
                    lines[i] = lines[i].replace(shorthandText, date);
                    modified = true;
                }
            }

            // 4. If changes were made, put the modified array back into the packet
            if (modified) {
                boolean success = setPacketTextField(packet, lines);
                if (!success) {
                    error("Could not modify UpdateSignC2SPacket. Please check your Minecraft version or report this issue.");
                }
            }

        } catch (Exception e) {
            error("Failed to process sign packet: " + e.getMessage());
        }
    }

    /**
     * Specifically for 1.21.1, tries to set the String[] field.
     * Field names can vary between mappings (e.g., 'text' or 'lines').
     */
    private boolean setPacketTextField(UpdateSignC2SPacket packet, String[] newLines) {
        // In 1.21.1 Yarn mappings, the field is 'text'.
        // In other mappings (like Mojang's official), it might be 'lines'.
        String[] possibleFieldNames = {"text", "lines"};

        for (String fieldName : possibleFieldNames) {
            try {
                Field field = packet.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);

                // Check if the field is actually a String[] to be safe
                if (field.getType().equals(String[].class)) {
                    field.set(packet, newLines);
                    return true; // Success!
                } else {
                    info("Field '" + fieldName + "' exists but is not a String[]. It is: " + field.getType().getSimpleName());
                }

            } catch (NoSuchFieldException e) {
                // Field not found with this name, try the next one.
            } catch (IllegalAccessException e) {
                error("Could not access field '" + fieldName + "': " + e.getMessage());
            }
        }

        // If we get here, neither field name worked.
        return false;
    }
}
