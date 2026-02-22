package com.example.addon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

import com.example.addon.AddonTemplate;

public class FluidPlacer extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<FluidType> fluid = sgGeneral.add(new EnumSetting.Builder<FluidType>()
        .name("fluid")
        .description("Type of fluid to place.")
        .defaultValue(FluidType.Lava)
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

    public FluidPlacer() {
        super(AddonTemplate.CATEGORY, "fluid-placer", "Automatically places fluid buckets on the face of blocks you look at.");
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

        // Get the block the player is looking at
        HitResult hit = mc.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult blockHit = (BlockHitResult) hit;

        // Place the fluid
        Item bucketItem = fluid.get() == FluidType.Lava ? Items.LAVA_BUCKET : Items.WATER_BUCKET;
        FindItemResult bucket = InvUtils.findInHotbar(bucketItem);
        if (!bucket.found()) {
            error("No " + fluid.get().name().toLowerCase() + " bucket in hotbar!");
            toggle();
            return;
        }

        InvUtils.swap(bucket.slot(), true);
        placeFluid(blockHit);
        InvUtils.swapBack();

        info("Placed " + fluid.get().name().toLowerCase());
    }

    private void placeFluid(BlockHitResult hitResult) {
        mc.interactionManager.interactBlock(
            mc.player,
            Hand.MAIN_HAND,
            hitResult
        );
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    public enum FluidType {
        Lava,
        Water
    }
}
