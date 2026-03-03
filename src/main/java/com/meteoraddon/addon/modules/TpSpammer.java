package com.meteoraddon.addon.modules;

import com.meteoraddon.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

import meteordevelopment.meteorclient.utils.player.ChatUtils;

import net.minecraft.world.border.WorldBorder;

import java.util.Random;

public class TpSpammer extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Delay between tp commands in ticks.")
        .defaultValue(20)
        .min(1)
        .build()
    );

    private final Setting<Integer> count = sgGeneral.add(new IntSetting.Builder()
        .name("count")
        .description("Number of tp commands to send.")
        .defaultValue(10)
        .min(1)
        .build()
    );

    private final Setting<Boolean> unlimited = sgGeneral.add(new BoolSetting.Builder()
        .name("unlimited")
        .description("Spam tp commands indefinitely until disabled.")
        .defaultValue(false)
        .build()
    );

    private final Random random = new Random();
    private int ticks = 0;
    private int sent = 0;

    public TpSpammer() {
        super(AddonTemplate.CATEGORY, "tp-spammer", "Spams /tp commands with random coordinates.");
    }

    @Override
    public void onActivate() {
        ticks = 0;
        sent = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!unlimited.get() && sent >= count.get()) {
            toggle(); // Disable after sending all
            return;
        }
        ticks++;
        if (ticks >= delay.get()) {
            ticks = 0;
            WorldBorder border = mc.world.getWorldBorder();
            double centerX = border.getCenterX();
            double centerZ = border.getCenterZ();
            double size = border.getSize();
            double minX = centerX - size / 2;
            double minZ = centerZ - size / 2;
            double x = minX + random.nextDouble() * size;
            double z = minZ + random.nextDouble() * size;
            int y = 320;
            ChatUtils.sendPlayerMsg("/tp " + (int)x + " " + y + " " + (int)z);
            if (!unlimited.get()) sent++;
        }
    }
}
