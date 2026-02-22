package com.meteoraddon.addon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import com.meteoraddon.addon.AddonTemplate;

public class LavaCast extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> pillarDelay = sgGeneral.add(new IntSetting.Builder()
        .name("pillar-delay")
        .description("Ticks between pillar blocks.")
        .defaultValue(2)
        .min(1)
        .max(20)
        .sliderMax(10)
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Ticks between lava and water.")
        .defaultValue(4)
        .min(1)
        .max(40)
        .sliderMax(20)
        .build()
    );

    private static final int PILLAR_HEIGHT = 2;

    private enum Step {
        PILLAR,
        WAIT_TOP,
        BREAK_GAP,
        PLACE_LAVA,
        WAIT_LAVA,
        PLACE_WATER,
        DONE
    }
    private Step     step;
    private int      tickTimer;
    private int      targetY;
    private BlockPos gapPos;
    private BlockPos capPos;
    public LavaCast() {
        super(AddonTemplate.CATEGORY, "lava-cast",
            "Builds tower, places lava then water.");
    }
    @Override
    public void onActivate() {
        if (mc.player == null) return;
        step      = Step.PILLAR;
        tickTimer = 0;
        targetY   = mc.player.getBlockPos().getY() + PILLAR_HEIGHT;
        if (!InvUtils.findInHotbar(stack ->
            stack.getItem() instanceof BlockItem bi &&
            bi.getBlock() != Blocks.AIR &&
            stack.getItem() != Items.LAVA_BUCKET &&
            stack.getItem() != Items.WATER_BUCKET
        ).found()) {
            error("No placeable blocks in hotbar!"); toggle(); return;
        }
        if (!InvUtils.findInHotbar(Items.LAVA_BUCKET).found()) {
            error("No lava bucket in hotbar!"); toggle(); return;
        }
        if (!InvUtils.findInHotbar(Items.WATER_BUCKET).found()) {
            error("No water bucket in hotbar!"); toggle(); return;
        }
        info("Pillaring to y=" + targetY);
    }
    @Override
    public void onDeactivate() {
        mc.options.jumpKey.setPressed(false);
    }
    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;
        switch (step) {
            case PILLAR -> {
                int currentY = mc.player.getBlockPos().getY();
                if (currentY >= targetY) {
                    capPos = mc.player.getBlockPos();
                    gapPos = mc.player.getBlockPos().down();
                    mc.options.jumpKey.setPressed(false);
                    tickTimer = 0;
                    step = Step.WAIT_TOP;
                    info("Tower built — breaking gap at y=" + gapPos.getY());
                    return;
                }
                if (++tickTimer < pillarDelay.get()) {
                    mc.options.jumpKey.setPressed(true);
                    return;
                }
                tickTimer = 0;
                BlockPos feet = mc.player.getBlockPos();
                BlockPos below = feet.down();
                if (mc.world.getBlockState(feet).isAir()) {
                    if (!placeBlock(below)) { error("Ran out of blocks!"); toggle(); return; }
                }
                mc.options.jumpKey.setPressed(true);
            }
            case WAIT_TOP -> {
                if (++tickTimer >= 2) { tickTimer = 0; step = Step.BREAK_GAP; }
            }
            case BREAK_GAP -> {
                if (mc.world.getBlockState(gapPos).isAir()) {
                    info("Gap opened, placing lava");
                    tickTimer = 0;
                    step = Step.PLACE_LAVA;
                    return;
                }
                if (tickTimer++ > 100) {
                    error("Failed to break gap block after 100 ticks");
                    toggle();
                    return;
                }
                mc.interactionManager.attackBlock(gapPos, Direction.DOWN);
                mc.player.swingHand(Hand.MAIN_HAND);
            }
            case PLACE_LAVA -> {
                FindItemResult lava = InvUtils.findInHotbar(Items.LAVA_BUCKET);
                if (!lava.found()) { error("Lava bucket gone!"); toggle(); return; }
                InvUtils.swap(lava.slot(), true);
                mc.interactionManager.interactBlock(
                    mc.player,
                    Hand.MAIN_HAND,
                    new BlockHitResult(
                        gapPos.down().toCenterPos().add(0, 0.5, 0),
                        Direction.UP,
                        gapPos.down(),
                        false
                    )
                );
                mc.player.swingHand(Hand.MAIN_HAND);
                InvUtils.swapBack();
                info("Lava placed, spreading");
                tickTimer = 0;
                step = Step.WAIT_LAVA;
            }
            case WAIT_LAVA -> { if (++tickTimer >= delay.get()) step = Step.PLACE_WATER; }
            case PLACE_WATER -> {
                FindItemResult water = InvUtils.findInHotbar(Items.WATER_BUCKET);
                if (!water.found()) { error("Water bucket gone!"); toggle(); return; }
                InvUtils.swap(water.slot(), true);
                mc.interactionManager.interactBlock(
                    mc.player,
                    Hand.MAIN_HAND,
                    new BlockHitResult(
                        capPos.down().toCenterPos().add(0, 0.5, 0),
                        Direction.UP,
                        capPos.down(),
                        false
                    )
                );
                mc.player.swingHand(Hand.MAIN_HAND);
                InvUtils.swapBack();
                info("Lavacast complete");
                step = Step.DONE;
                toggle();
            }
            case DONE -> toggle();
        }
    }
    private boolean placeBlock(BlockPos pos) {
        if (!mc.world.getBlockState(pos).isAir()) return true;
        FindItemResult block = InvUtils.findInHotbar(stack ->
            stack.getItem() instanceof BlockItem bi &&
            bi.getBlock() != Blocks.AIR &&
            stack.getItem() != Items.LAVA_BUCKET &&
            stack.getItem() != Items.WATER_BUCKET
        );
        if (!block.found()) return false;
        InvUtils.swap(block.slot(), true);
        mc.interactionManager.interactBlock(
            mc.player,
            Hand.MAIN_HAND,
            new BlockHitResult(
                pos.toCenterPos().add(0, 0.5, 0),
                Direction.UP,
                pos,
                false
            )
        );
        mc.player.swingHand(Hand.MAIN_HAND);
        InvUtils.swapBack();
        return true;
    }
}