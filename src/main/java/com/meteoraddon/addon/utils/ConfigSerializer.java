package com.meteoraddon.addon.utils;

import java.nio.file.Path;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;

public class ConfigSerializer {
    public static void writeToJson(Path path, String type, Pair<BlockPos, Vec3d> cartographyTable, Pair<BlockPos, Vec3d> finishedMapChest, Pair<BlockPos, Vec3d> bed, Pair<BlockPos, Vec3d> usedToolChest, ArrayList<Pair<BlockPos, Vec3d>> mapMaterialChests, Pair<Vec3d, Pair<Float, Float>> dumpStation, BlockPos mapCorner, HashMap<Item, ArrayList<Pair<BlockPos, Vec3d>>> materialDict, Set<ItemStack> toolSet) {
        // Stub implementation
    }
}
