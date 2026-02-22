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

public class ConfigDeserializer {
    public static class ConfigData {
        public String type;
        public Pair<BlockPos, Vec3d> cartographyTable;
        public Pair<BlockPos, Vec3d> finishedMapChest;
        public Pair<Vec3d, Pair<Float, Float>> dumpStation;
        public BlockPos mapCorner;
        public HashMap<Item, ArrayList<Pair<BlockPos, Vec3d>>> materialDict;
        public Pair<BlockPos, Vec3d> usedToolChest;
        public Pair<BlockPos, Vec3d> bed;
        public ArrayList<Pair<BlockPos, Vec3d>> mapMaterialChests;
        public Set<ItemStack> toolSet;
    }

    public static ConfigData readFromJson(Path path) {
        // Stub implementation
        return new ConfigData();
    }
}
