package com.example.addon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

public class AutoStaircase extends Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgToggles = settings.createGroup("Toggles");

    public final Setting<Integer> height = sgGeneral.add(new IntSetting.Builder()
        .name("height")
        .description("Height of the staircase.")
        .defaultValue(50)
        .min(1)
        .sliderRange(1, 100)
        .build()
    );

    private final Setting<Item> stairItem = sgGeneral.add(new ItemSetting.Builder()
        .name("stair-item")
        .description("Item to use for stairs.")
        .defaultValue(Items.STONE)
        .build()
    );

    private final Setting<Boolean> debug = sgToggles.add(new BoolSetting.Builder()
        .name("debug")
        .description("Enable debug messages.")
        .defaultValue(false)
        .build()
    );

    private int currentHeight = 0;

    public AutoStaircase() {
        super(Categories.World, "auto-staircase", "Automatically builds a staircase with any block.");
    }

    @Override
    public void onActivate() {
        currentHeight = 0;
        info("Starting staircase build");
    }

    @Override
    public void onDeactivate() {
        info("Staircase build stopped at height " + currentHeight);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (currentHeight >= height.get()) {
            toggle();
            return;
        }

        // Place blocks relative to player's current position
        BlockPos playerPos = mc.player.getBlockPos();
        
        // First block of the step (at current height, 1 block forward)
        BlockPos stairPos = playerPos.offset(mc.player.getHorizontalFacing(), 1).up(currentHeight);
        
        // Second block of the step (at current height, 2 blocks forward)
        BlockPos secondPos = playerPos.offset(mc.player.getHorizontalFacing(), 2).up(currentHeight);

        // Find the stair item
        FindItemResult result = InvUtils.find(stairItem.get());
        
        if (!result.found()) {
            warning("Out of stair blocks!");
            toggle();
            return;
        }

        // Place both blocks for this step
        boolean placed1 = BlockUtils.place(stairPos, result, false, 0, true);
        boolean placed2 = BlockUtils.place(secondPos, result, false, 0, true);

        if (placed1 && placed2) {
            if (debug.get()) info("Placed step " + currentHeight + " at " + stairPos + " and " + secondPos);
            currentHeight++;
        } else if (placed1 || placed2) {
            if (debug.get()) info("Partially placed step " + currentHeight);
        } else {
            if (debug.get()) warning("Failed to place - checking position...");
        }
    }

    @Override
    public String getInfoString() {
        return String.valueOf(currentHeight);
    }
}