package com.meteoraddon.addon.modules;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import com.meteoraddon.addon.AddonTemplate;

public class AutoPyramid extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> levels = sgGeneral.add(new IntSetting.Builder()
        .name("levels")
        .description("Number of levels for the pyramid.")
        .defaultValue(5)
        .min(1)
        .max(20)
        .sliderMax(20)
        .build()
    );

    public AutoPyramid() {
        super(AddonTemplate.CATEGORY, "auto-pyramid", "Automatically builds a pyramid forward from your position.");
    }

    @Override
    public void onActivate() {
        if (mc.player == null || mc.world == null) {
            toggle();
            return;
        }

        buildPyramid();
        toggle();
    }

    private void buildPyramid() {
        BlockPos basePos = mc.player.getBlockPos();
        Direction direction = mc.player.getHorizontalFacing();

        // Find blocks
        FindItemResult blocks = InvUtils.findInHotbar(Items.COBBLESTONE);
        if (!blocks.found()) {
            blocks = InvUtils.findInHotbar(Items.STONE);
            if (!blocks.found()) {
                error("No cobblestone or stone in hotbar.");
                return;
            }
        }

        InvUtils.swap(blocks.slot(), true);

        for (int level = 0; level < levels.get(); level++) {
            int size = levels.get() - level; // Size decreases with height
            int y = level;

            for (int x = -size / 2; x <= size / 2; x++) {
                for (int z = -size / 2; z <= size / 2; z++) {
                    BlockPos pos = basePos.add(direction.getVector().multiply(x)).add(0, y, 0).add(0, 0, z);
                    if (!placeBlock(pos)) {
                        error("Failed to place block at " + pos);
                    }
                }
            }
        }

        InvUtils.swapBack();
        info("Pyramid built (" + levels.get() + " levels)");
    }

    private boolean placeBlock(BlockPos pos) {
        Direction dir = findAdjacentSolid(pos);
        if (dir == null) {
            return false;
        }

        BlockPos solidPos = pos.offset(dir);
        Direction clickSide = dir.getOpposite();

        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
            new BlockHitResult(Vec3d.ofCenter(solidPos), clickSide, solidPos, false));
        mc.player.swingHand(Hand.MAIN_HAND);
        return true;
    }

    private Direction findAdjacentSolid(BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos adj = pos.offset(dir);
            if (!mc.world.getBlockState(adj).isAir() &&
                mc.world.getBlockState(adj).isSolidBlock(mc.world, adj)) {
                return dir;
            }
        }
        return null;
    }
}
