package com.meteoraddon.addon.utils;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class WorldUtils {
    /**
     * Finds the first air block in the player's line of sight within the given range.
     */
    public static BlockPos findAirBlockInSight(World world, Vec3d eyePos, Vec3d direction, double range) {
        Vec3d end = eyePos.add(direction.multiply(range));
        double step = 0.1;
        double distance = eyePos.distanceTo(end);

        for (double d = 0; d < distance; d += step) {
            Vec3d pos = eyePos.add(direction.multiply(d));
            BlockPos blockPos = BlockPos.ofFloored(pos);
            if (world.getBlockState(blockPos).isAir()) {
                return blockPos;
            }
        }
        return null;
    }

    /**
     * Checks if a position is within the world height limits.
     */
    public static boolean isValidY(int y) {
        return y >= -64 && y <= 320; // Assuming standard Minecraft height limits
    }

    /**
     * Gets the top solid block at the given x,z coordinates.
     */
    public static BlockPos getTopSolidBlock(World world, int x, int z) {
        for (int y = world.getHeight() - 1; y >= world.getBottomY(); y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (BlockUtils.isSolid(world, pos)) {
                return pos;
            }
        }
        return null;
    }
}
