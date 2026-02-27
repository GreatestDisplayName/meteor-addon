package com.meteoraddon.addon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.Identifier;

import com.meteoraddon.addon.AddonTemplate;

public class SpeedHaxPlus extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> speedMultiplier = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed-multiplier")
        .description("Multiplier for movement speed.")
        .defaultValue(2.0)
        .min(1.0)
        .max(10.0)
        .sliderMax(10.0)
        .build()
    );

    private final Identifier speedModifierId = Identifier.of("meteoraddon", "speed_hax_plus");

    public SpeedHaxPlus() {
        super(AddonTemplate.CATEGORY, "speed-hax-plus", "Increases player movement speed.");
    }

    @Override
    public void onActivate() {
        if (mc.player != null) {
            mc.player.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).addPersistentModifier(
                new EntityAttributeModifier(speedModifierId, speedMultiplier.get() - 1.0, Operation.ADD_MULTIPLIED_TOTAL)
            );
        }
    }

    @Override
    public void onDeactivate() {
        if (mc.player != null) {
            mc.player.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).removeModifier(speedModifierId);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        // Update the modifier value if setting changed
        if (mc.player != null) {
            mc.player.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).removeModifier(speedModifierId);
            mc.player.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).addPersistentModifier(
                new EntityAttributeModifier(speedModifierId, speedMultiplier.get() - 1.0, Operation.ADD_MULTIPLIED_TOTAL)
            );
        }
    }
}
