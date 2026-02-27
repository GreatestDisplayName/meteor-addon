package com.meteoraddon.addon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import com.meteoraddon.addon.AddonTemplate;

public class MineNotStone extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("range")
        .description("Mining range.")
        .defaultValue(5)
        .min(1)
        .max(10)
        .sliderMax(10)
        .build()
    );

    private final Setting<Integer> crustHeight = sgGeneral.add(new IntSetting.Builder()
        .name("crust-height")
        .description("Maximum Y level for mining (first crust).")
        .defaultValue(5)
        .min(0)
        .max(10)
        .sliderMax(10)
        .build()
    );

    private boolean building = false;
    private int buildY = 0;

    public MineNotStone() {
        super(AddonTemplate.CATEGORY, "mine-not-stone", "Mines non-stone blocks on the surface, builds to limit when inventory full, then dies.");
    }

    @Override
    public void onActivate() {
        building = false;
        buildY = (int) mc.player.getY();
    }

    @Override
    public void onDeactivate() {
        building = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        if (!building) {
            // Mining mode
            if (!InvUtils.findEmpty().found()) {
                building = true;
                buildY = (int) mc.player.getY();
                info("Inventory full, switching to building mode.");
                return;
            }

            // Find block to mine
            BlockPos target = null;
            double closestDist = range.get();
            for (int x = -range.get(); x <= range.get(); x++) {
                for (int y = 0; y <= crustHeight.get(); y++) {
                    for (int z = -range.get(); z <= range.get(); z++) {
                        BlockPos pos = mc.player.getBlockPos().add(x, y, z);
                        Block block = mc.world.getBlockState(pos).getBlock();
                        if (block != Blocks.STONE && block != Blocks.AIR && block != Blocks.WATER && block != Blocks.LAVA) {
                            double dist = mc.player.getBlockPos().getSquaredDistance(pos);
                            if (dist < closestDist * closestDist) {
                                closestDist = dist;
                                target = pos;
                            }
                        }
                    }
                }
            }

            if (target != null) {
                // Mine the block
                mc.interactionManager.attackBlock(target, Direction.UP);
                mc.player.swingHand(mc.player.getActiveHand());
            }
        } else {
            // Building mode
            if (buildY >= 320) {
                // Jump off and die
                mc.player.setVelocity(0, -10, 0);
                mc.player.setHealth(0f);
                toggle();
                return;
            }

            // Place block at buildY + 1
            BlockPos placePos = new BlockPos(mc.player.getBlockX(), buildY + 1, mc.player.getBlockZ());
            if (mc.world.getBlockState(placePos).getBlock() == Blocks.AIR) {
                // Find block in inventory
                FindItemResult result = InvUtils.findInHotbar(item -> !item.isEmpty() && Block.getBlockFromItem(item.getItem()) != Blocks.AIR);
                if (result.found()) {
                    int slot = result.slot();
                    InvUtils.swap(slot, true);
                    BlockPos below = new BlockPos(mc.player.getBlockX(), buildY, mc.player.getBlockZ());
                    BlockHitResult hit = new BlockHitResult(below.toCenterPos().add(0, 1, 0), Direction.UP, below, false);
                    mc.interactionManager.interactBlock(mc.player, mc.player.getActiveHand(), hit);
                    mc.player.swingHand(mc.player.getActiveHand());
                    InvUtils.swapBack();
                    buildY++;
                } else {
                    error("No blocks to place.");
                    toggle();
                }
            } else {
                buildY++;
            }
        }
    }
}
