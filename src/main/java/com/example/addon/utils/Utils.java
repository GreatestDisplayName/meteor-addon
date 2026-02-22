package com.example.addon.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.block.Block;
import net.minecraft.nbt.NbtList;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Collection;

public class Utils {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static File getMinecraftDirectory() {
        return new File(System.getProperty("user.home"), ".minecraft");
    }

    public static boolean createFolders(File folder) {
        return folder.exists() || folder.mkdirs();
    }

    public static int getIntervalStart(int coord) {
        return coord & ~127;
    }

    public static void setForwardPressed(boolean b) {
        mc.options.forwardKey.setPressed(b);
    }

    public static void setBackwardPressed(boolean b) {
        mc.options.backKey.setPressed(b);
    }

    public static void setJumpPressed(boolean b) {
        mc.options.jumpKey.setPressed(b);
    }

    public static ArrayList<Pair<BlockPos, Vec3d>> saveAdd(ArrayList<Pair<BlockPos, Vec3d>> list, BlockPos pos, Vec3d vec) {
        list.add(new Pair<>(pos, vec));
        return list;
    }

    public static int findHighestFreeSlot(InventoryS2CPacket packet) {
        List<ItemStack> contents = packet.contents();
        for (int i = contents.size() - 1; i >= 0; i--) {
            if (contents.get(i).isEmpty()) return i;
        }
        return -1;
    }

    public static void getOneItem(int slot, boolean hotbar, List<Integer> availableSlots, List<Integer> availableHotBarSlots, InventoryS2CPacket packet) {
        // Stub implementation
    }

    public static void dumpItems() {
        // Stub
    }

    public static void walkTo(Vec3d pos, Object sprintMode) {
        // Stub
    }

    public static Direction getInteractionSide(BlockPos pos) {
        return Direction.UP; // Stub
    }

    public static ArrayList<Integer> getAvailableSlots(HashMap<Item, ArrayList<Pair<BlockPos, Vec3d>>> map) {
        return new ArrayList<>(); // Stub
    }

    public static Pair<ArrayList<Integer>, HashMap<Item, Integer>> getInvInformation(HashMap<Item, Integer> map, ArrayList<Integer> list) {
        return new Pair<>(new ArrayList<>(), new HashMap<>()); // Stub
    }

    public static int stacksRequired(Collection<Integer> collection) {
        return 0; // Stub
    }

    public static File getNextMapFile(File folder, ArrayList<File> files, Boolean bool) {
        return null; // Stub
    }

    public static HashMap<Integer, Pair<Block, Integer>> getBlockPalette(NbtList list) {
        return new HashMap<>(); // Stub
    }
}
