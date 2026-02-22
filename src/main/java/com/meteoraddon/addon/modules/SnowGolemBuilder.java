package com.meteoraddon.addon.modules;

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

public class SnowGolemBuilder extends Module {

    public SnowGolemBuilder() {
        super(AddonTemplate.CATEGORY, "snow-golem-builder", "Automatically builds a snow golem at your position.");
    }

    @Override
    public void onActivate() {
        if (mc.player == null || mc.world == null) {
            toggle();
            return;
        }

        buildSnowGolem();
        toggle();
    }

    private void buildSnowGolem() {
        BlockPos basePos = mc.player.getBlockPos();

        // Check if positions are air
        if (!mc.world.getBlockState(basePos).isAir() ||
            !mc.world.getBlockState(basePos.up()).isAir() ||
            !mc.world.getBlockState(basePos.up(2)).isAir()) {
            error("Positions not clear for snow golem.");
            return;
        }

        // Find snow blocks
        FindItemResult snow = InvUtils.findInHotbar(Items.SNOW_BLOCK);
        if (!snow.found()) {
            error("No snow blocks in hotbar.");
            return;
        }

        // Find pumpkin
        FindItemResult pumpkin = InvUtils.findInHotbar(Items.CARVED_PUMPKIN);
        if (!pumpkin.found()) {
            error("No carved pumpkin in hotbar.");
            return;
        }

        // Place bottom snow
        InvUtils.swap(snow.slot(), true);
        if (!placeBlock(basePos)) return;

        // Place middle snow
        if (!placeBlock(basePos.up())) return;

        // Place pumpkin
        InvUtils.swap(pumpkin.slot(), true);
        if (!placeBlock(basePos.up(2))) return;

        InvUtils.swapBack();
        info("Snow golem built.");
    }

    private boolean placeBlock(BlockPos pos) {
        Direction dir = findAdjacentSolid(pos);
        if (dir == null) {
            error("No solid block adjacent to " + pos);
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
