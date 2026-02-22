package com.example.addon.modules;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import com.example.addon.AddonTemplate;

public class IronGolemBuilder extends Module {

    public IronGolemBuilder() {
        super(AddonTemplate.CATEGORY, "iron-golem-builder", "Automatically builds an iron golem at your position.");
    }

    @Override
    public void onActivate() {
        if (mc.player == null || mc.world == null) {
            toggle();
            return;
        }

        buildIronGolem();
        toggle();
    }

    private void buildIronGolem() {
        BlockPos base = mc.player.getBlockPos().down();

        BlockPos[] positions = {
            base.add(-1, 0, 0), base, base.add(1, 0, 0), // bottom row
            base.add(0, 1, 0), // middle
            base.add(0, 2, 0), // top
            base.add(0, 3, 0)  // pumpkin
        };

        // Check all positions are air
        for (BlockPos pos : positions) {
            if (!mc.world.getBlockState(pos).isAir()) {
                error("Position " + pos + " not clear for iron golem.");
                return;
            }
        }

        // Find iron blocks (need at least 4)
        FindItemResult iron = InvUtils.findInHotbar(Items.IRON_BLOCK);
        if (!iron.found()) {
            error("No iron blocks in hotbar.");
            return;
        }

        // Find pumpkin
        FindItemResult pumpkin = InvUtils.findInHotbar(Items.CARVED_PUMPKIN);
        if (!pumpkin.found()) {
            error("No carved pumpkin in hotbar.");
            return;
        }

        // Place bottom row
        InvUtils.swap(iron.slot(), true);
        for (int i = 0; i < 3; i++) {
            if (!placeBlock(positions[i])) return;
        }

        // Place middle iron
        if (!placeBlock(positions[3])) return;

        // Place top iron
        if (!placeBlock(positions[4])) return;

        // Place pumpkin
        InvUtils.swap(pumpkin.slot(), true);
        if (!placeBlock(positions[5])) return;

        InvUtils.swapBack();
        info("Iron golem built.");
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
