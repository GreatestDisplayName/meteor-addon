package com.example.addon.utils;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public class MapAreaCache {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void reset(BlockPos corner) {
        // Stub implementation
    }

    public static BlockState getCachedBlockState(BlockPos pos) {
        return mc.world.getBlockState(pos);
    }
}
