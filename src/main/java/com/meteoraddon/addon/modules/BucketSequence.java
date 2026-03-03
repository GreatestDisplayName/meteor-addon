package com.meteoraddon.addon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import com.meteoraddon.addon.AddonTemplate;

public class BucketSequence extends Module {
    private enum State {
        PLACE_LAVA,
        WAIT_LAVA_TO_GROUND,
        PICKUP_LAVA,
        WAIT_LAVA_TO_TARGET,
        PLACE_WATER,
        WAIT_WATER_LEAD,
        PICKUP_WATER,
        INCREMENT_AND_LOOP
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> baseLavaTicks = sgGeneral.add(new IntSetting.Builder()
        .name("base-lava-ticks")
        .description("Base ticks for lava to flow from build limit to ground (256 blocks = 7680 ticks)")
        .defaultValue(7680)
        .min(0)
        .sliderMax(20000)
        .build()
    );

    private final Setting<Integer> incrementPerCycle = sgGeneral.add(new IntSetting.Builder()
        .name("increment-per-cycle")
        .description("Ticks added to lava wait time each cycle (e.g., 30 ticks per block lower)")
        .defaultValue(30)
        .min(0)
        .sliderMax(1000)
        .build()
    );

    private final Setting<Integer> targetHeightTicks = sgGeneral.add(new IntSetting.Builder()
        .name("target-height-ticks")
        .description("Ticks to wait after picking up lava for it to hit target height")
        .defaultValue(0)
        .min(0)
        .sliderMax(10000)
        .build()
    );

    private final Setting<Integer> waterLeadTicks = sgGeneral.add(new IntSetting.Builder()
        .name("water-lead-ticks")
        .description("Ticks to wait after placing water for a 3‑block lead (3 blocks = 15 ticks)")
        .defaultValue(15)
        .min(0)
        .sliderMax(1000)
        .build()
    );

    private State state = State.PLACE_LAVA;
    private int tickCounter = 0;
    private int currentLavaTicks;
    private BlockPos lavaPos;
    private int lavaBucketSlot = -1;
    private BlockPos waterPos;
    private int waterBucketSlot = -1;

    public BucketSequence() {
        super(AddonTemplate.CATEGORY, "bucket-sequence", "Automated lava casting sequence with increasing delay.");
    }

    @Override
    public void onActivate() {
        reset();
        currentLavaTicks = baseLavaTicks.get();
        state = State.PLACE_LAVA;
        tickCounter = 0;
    }

    @Override
    public void onDeactivate() {
        // Cleanup if needed
    }

    private void reset() {
        lavaPos = null;
        waterPos = null;
        lavaBucketSlot = -1;
        waterBucketSlot = -1;
    }

    private void placeLava() {
        FindItemResult lava = InvUtils.findInHotbar(item -> item.getItem() == Items.LAVA_BUCKET);
        if (!lava.found()) {
            error("No lava bucket in hotbar");
            toggle();
            return;
        }

        if (!(mc.crosshairTarget instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
            error("Not looking at a block");
            toggle();
            return;
        }

        BlockPos targetPos = hit.getBlockPos().offset(hit.getSide());

        if (mc.world.getBlockState(targetPos).getBlock() instanceof FluidBlock) {
            error("Cannot place on fluid");
            toggle();
            return;
        }

        lavaBucketSlot = lava.slot();
        InvUtils.swap(lavaBucketSlot, true);

        BlockHitResult placeHit = new BlockHitResult(Vec3d.ofCenter(targetPos), hit.getSide().getOpposite(), targetPos, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, placeHit);

        // Verify lava was placed
        if (mc.world.getBlockState(targetPos).getBlock() != Blocks.LAVA) {
            InvUtils.swapBack();
            error("Failed to place lava bucket (block did not become lava)");
            toggle();
            return;
        }

        if (mc.player.getInventory().getStack(lavaBucketSlot).getItem() != Items.BUCKET) {
            InvUtils.swapBack();
            error("Bucket did not empty – placement may have failed");
            toggle();
            return;
        }

        InvUtils.swapBack();

        lavaPos = targetPos;
        state = State.WAIT_LAVA_TO_GROUND;
        tickCounter = 0;
        info("Lava placed at " + targetPos + ". Waiting " + (currentLavaTicks / 20) + "s for ground reach.");
    }

    private void pickupLava() {
        if (lavaPos == null) {
            error("Lava position lost");
            toggle();
            return;
        }

        // Ensure we still have an empty bucket in the same slot (it became empty after placing)
        if (lavaBucketSlot == -1 || mc.player.getInventory().getStack(lavaBucketSlot).getItem() != Items.BUCKET) {
            // Try to find any empty bucket
            FindItemResult empty = InvUtils.findInHotbar(Items.BUCKET);
            if (!empty.found()) {
                error("No empty bucket to pick up lava");
                toggle();
                return;
            }
            lavaBucketSlot = empty.slot();
        }

        // Look at the lava source block
        if (!(mc.world.getBlockState(lavaPos).isOf(Blocks.LAVA))) {
            error("Lava source no longer there");
            toggle();
            return;
        }

        // Right‑click with empty bucket
        InvUtils.swap(lavaBucketSlot, true);
        BlockHitResult pickupHit = new BlockHitResult(Vec3d.ofCenter(lavaPos), Direction.UP, lavaPos, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, pickupHit);
        InvUtils.swapBack();

        // After pickup, the bucket becomes lava again, but we don't need the slot now
        lavaBucketSlot = -1;

        state = State.WAIT_LAVA_TO_TARGET;
        tickCounter = 0;
        info("Lava picked up. Waiting " + targetHeightTicks.get() + " ticks for target height.");
    }

    private void placeWater() {
        if (lavaPos == null) {
            error("Lava position missing");
            toggle();
            return;
        }

        FindItemResult water = InvUtils.findInHotbar(item -> item.getItem() == Items.WATER_BUCKET);
        if (!water.found()) {
            error("No water bucket in hotbar");
            toggle();
            return;
        }

        BlockPos waterPos = lavaPos.add(1, 0, 0); // place east of lava source

        if (mc.world.getBlockState(waterPos).getBlock() instanceof FluidBlock) {
            error("Cannot place water on fluid");
            toggle();
            return;
        }

        waterBucketSlot = water.slot();
        InvUtils.swap(waterBucketSlot, true);

        BlockHitResult waterHit = new BlockHitResult(Vec3d.ofCenter(waterPos), Direction.DOWN, waterPos, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, waterHit);

        // Verify water was placed
        if (mc.world.getBlockState(waterPos).getBlock() != Blocks.WATER) {
            InvUtils.swapBack();
            error("Failed to place water bucket");
            toggle();
            return;
        }

        if (mc.player.getInventory().getStack(waterBucketSlot).getItem() != Items.BUCKET) {
            InvUtils.swapBack();
            error("Water bucket did not empty");
            toggle();
            return;
        }

        InvUtils.swapBack();

        this.waterPos = waterPos;
        state = State.WAIT_WATER_LEAD;
        tickCounter = 0;
        info("Water placed at " + waterPos + ". Waiting " + waterLeadTicks.get() + " ticks for lead.");
    }

    private void pickupWater() {
        if (waterPos == null) {
            error("Water position lost");
            toggle();
            return;
        }

        // Find empty bucket (the water bucket became empty after placing)
        if (waterBucketSlot == -1 || mc.player.getInventory().getStack(waterBucketSlot).getItem() != Items.BUCKET) {
            FindItemResult empty = InvUtils.findInHotbar(Items.BUCKET);
            if (!empty.found()) {
                error("No empty bucket to pick up water");
                toggle();
                return;
            }
            waterBucketSlot = empty.slot();
        }

        // Check water still there
        if (!(mc.world.getBlockState(waterPos).isOf(Blocks.WATER))) {
            error("Water source no longer there");
            toggle();
            return;
        }

        InvUtils.swap(waterBucketSlot, true);
        BlockHitResult pickupHit = new BlockHitResult(Vec3d.ofCenter(waterPos), Direction.UP, waterPos, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, pickupHit);
        InvUtils.swapBack();

        waterBucketSlot = -1;
        waterPos = null;

        state = State.INCREMENT_AND_LOOP;
        tickCounter = 0;
    }

    private void incrementAndLoop() {
        currentLavaTicks += incrementPerCycle.get();
        info("Cycle complete. New lava wait: " + (currentLavaTicks / 20) + "s");
        state = State.PLACE_LAVA;
        tickCounter = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;

        tickCounter++;

        switch (state) {
            case PLACE_LAVA:
                placeLava();
                break;

            case WAIT_LAVA_TO_GROUND:
                if (tickCounter >= currentLavaTicks) {
                    state = State.PICKUP_LAVA;
                    tickCounter = 0;
                }
                break;

            case PICKUP_LAVA:
                pickupLava();
                break;

            case WAIT_LAVA_TO_TARGET:
                if (tickCounter >= targetHeightTicks.get()) {
                    state = State.PLACE_WATER;
                    tickCounter = 0;
                }
                break;

            case PLACE_WATER:
                placeWater();
                break;

            case WAIT_WATER_LEAD:
                if (tickCounter >= waterLeadTicks.get()) {
                    state = State.PICKUP_WATER;
                    tickCounter = 0;
                }
                break;

            case PICKUP_WATER:
                pickupWater();
                break;

            case INCREMENT_AND_LOOP:
                incrementAndLoop();
                break;
        }
    }
}