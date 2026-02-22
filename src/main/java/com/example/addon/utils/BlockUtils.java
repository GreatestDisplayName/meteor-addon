package com.example.addon.utils;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockUtils {
    /**
     * Checks if the block at the given position is air.
     */
    public static boolean isAir(World world, BlockPos pos) {
        return world.getBlockState(pos).isAir();
    }

    /**
     * Checks if the block at the given position is solid.
     */
    public static boolean isSolid(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return !state.isAir() && state.isSolidBlock(world, pos);
    }

    /**
     * Gets the hardness of the block at the given position.
     */
    public static float getHardness(World world, BlockPos pos) {
        return world.getBlockState(pos).getHardness(world, pos);
    }
}
