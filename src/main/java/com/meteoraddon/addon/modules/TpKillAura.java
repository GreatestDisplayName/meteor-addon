package com.meteoraddon.addon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import com.meteoraddon.addon.AddonTemplate;

public class TpKillAura extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("Maximum range to teleport and attack targets.")
        .defaultValue(10.0)
        .min(1.0)
        .max(100.0)
        .sliderMax(100.0)
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Ticks between attacks.")
        .defaultValue(10)
        .min(1)
        .max(100)
        .sliderMax(100)
        .build()
    );

    private int timer = 0;
    private Vec3d originalPos;

    public TpKillAura() {
        super(AddonTemplate.CATEGORY, "tp-kill-aura", "Automatically teleports to and attacks nearby enemies.");
    }

    @Override
    public void onActivate() {
        timer = 0;
        if (mc.player != null) {
            originalPos = new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        }
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

        // Teleport to target
        Vec3d targetPos = new Vec3d(target.getX(), target.getY(), target.getZ());
        mc.player.setPosition(targetPos.x, targetPos.y, targetPos.z);

        // Attack
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);

        // Teleport back
        mc.player.setPosition(originalPos.x, originalPos.y, originalPos.z);

        info("Attacked " + target.getName().getString() + " with TP Kill Aura");
    }
}
