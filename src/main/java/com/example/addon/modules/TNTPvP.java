package com.example.addon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

import com.example.addon.AddonTemplate;

public class TNTPvP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("Maximum range to place TNT near targets.")
        .defaultValue(6.0)
        .min(1.0)
        .max(12.0)
        .sliderMax(12.0)
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Ticks between placements.")
        .defaultValue(20)
        .min(1)
        .max(100)
        .sliderMax(100)
        .build()
    );

    private int timer = 0;

    public TNTPvP() {
        super(AddonTemplate.CATEGORY, "tnt-pvp", "Automatically places TNT near nearby enemies in PvP.");
    }

    @Override
    public void onActivate() {
        timer = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        timer++;
        if (timer < delay.get()) return;
        timer = 0;

        // Find closest player target
        Entity target = null;
        double closestDist = range.get();
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (entity.getType() != EntityType.PLAYER) continue;
            double dist = mc.player.distanceTo(entity);
            if (dist < closestDist) {
                closestDist = dist;
                target = entity;
            }
        }

        if (target == null) return;

        // Throw TNT
        FindItemResult tnt = InvUtils.findInHotbar(Items.TNT);
        if (!tnt.found()) {
            error("No TNT in hotbar.");
            toggle();
            return;
        }

        InvUtils.swap(tnt.slot(), true);
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.swingHand(Hand.MAIN_HAND);
        InvUtils.swapBack();

        info("Threw TNT at " + target.getName().getString());
    }
}
