package com.meteoraddon.addon.modules;

import com.meteoraddon.addon.AddonTemplate;
import com.meteoraddon.addon.utils.TextDebug;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoWitherBuilder extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("build-delay")
        .description("The delay between placing blocks in ticks.")
        .defaultValue(2)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Automatically rotates your camera to face the blocks being placed.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> render = sgGeneral.add(new BoolSetting.Builder()
        .name("render")
        .description("Renders a preview of where the wither will be built.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> soulSandColor = sgGeneral.add(new ColorSetting.Builder()
        .name("soul-sand-color")
        .description("The color of the soul sand preview.")
        .defaultValue(new SettingColor(139, 69, 19))
        .visible(render::get)
        .build()
    );

    private final Setting<SettingColor> skullColor = sgGeneral.add(new ColorSetting.Builder()
        .name("skull-color")
        .description("The color of the skull preview.")
        .defaultValue(new SettingColor(200, 200, 200))
        .visible(render::get)
        .build()
    );

    public AutoWitherBuilder() {
        super(AddonTemplate.CATEGORY, "auto-wither-builder", "Builds a Wither at your feet.");
    }

    private static final BlockPos[] SOUL_SAND_POS = new BlockPos[] {
        new BlockPos(0, 0, -2),
        new BlockPos(0, 1, -2),
        new BlockPos(-1, 1, -2),
        new BlockPos(1, 1, -2)
    };

    private static final BlockPos[] SKULL_POS = new BlockPos[] {
        new BlockPos(0, 2, -2),
        new BlockPos(-1, 2, -2),
        new BlockPos(1, 2, -2)
    };

    private int timer;
    private int stage;
    private BlockPos startPos;
    private Direction direction;

    @Override
    public void onActivate() {
        TextDebug.module("AutoWitherBuilder", "Activated with delay: %d ticks", delay.get());
        timer = 0;
        stage = 0;
        startPos = mc.player.getBlockPos();
        direction = getDirectionFromYaw(mc.player.getYaw());
        TextDebug.module("AutoWitherBuilder", "Start position: %s, Direction: %s", startPos, direction.name());
    }

    private Direction getDirectionFromYaw(float yaw) {
        float normalizedYaw = (yaw % 360 + 360) % 360;
        if (normalizedYaw >= 45 && normalizedYaw < 135) {
            return Direction.EAST;
        } else if (normalizedYaw >= 135 && normalizedYaw < 225) {
            return Direction.SOUTH;
        } else if (normalizedYaw >= 225 && normalizedYaw < 315) {
            return Direction.WEST;
        } else {
            return Direction.NORTH;
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!render.get() || stage == 0) return;

        for (BlockPos pos : SOUL_SAND_POS) {
            BlockPos transformed = transform(pos);
            event.renderer.box(transformed, soulSandColor.get(), soulSandColor.get(), ShapeMode.Lines, 0);
        }

        for (BlockPos pos : SKULL_POS) {
            BlockPos transformed = transform(pos);
            event.renderer.box(transformed, skullColor.get(), skullColor.get(), ShapeMode.Lines, 0);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (timer > 0) {
            timer--;
            return;
        }

        FindItemResult soulSand = InvUtils.findInHotbar(Items.SOUL_SAND);
        if (!soulSand.found()) {
            error("No soul sand in hotbar.");
            toggle();
            return;
        }

        FindItemResult skulls = InvUtils.findInHotbar(Items.WITHER_SKELETON_SKULL);
        if (!skulls.found() || skulls.count() < 3) {
            error("Not enough wither skulls in hotbar.");
            toggle();
            return;
        }

        if (stage == 0) {
            for (BlockPos pos : SOUL_SAND_POS) {
                if (!canPlace(transform(pos))) {
                    error("Not enough space to build the Wither here.");
                    toggle();
                    return;
                }
            }
            for (BlockPos pos : SKULL_POS) {
                if (!canPlace(transform(pos))) {
                    error("Not enough space to build the Wither here.");
                    toggle();
                    return;
                }
            }
            stage++;
        }

        BlockPos now;
        FindItemResult item;

        if (stage >= 1 && stage <= 4) {
            now = transform(SOUL_SAND_POS[stage - 1]);
            item = soulSand;
        } else if (stage >= 5 && stage <= 7) {
            now = transform(SKULL_POS[stage - 5]);
            item = skulls;
        } else {
            info("Wither built successfully.");
            toggle();
            return;
        }

        if (rotate.get()) {
            Rotations.rotate(Rotations.getYaw(now), Rotations.getPitch(now), () -> place(now, item));
        } else {
            place(now, item);
        }

        timer = delay.get();
        stage++;
    }

    private void place(BlockPos pos, FindItemResult item) {
        if (!canPlace(pos)) return;
        BlockUtils.place(pos, item, 0, false);
    }

    private boolean canPlace(BlockPos pos) {
        return mc.world.getBlockState(pos).isReplaceable();
    }

    private BlockPos transform(BlockPos p) {
        switch (direction) {
            case EAST:  return startPos.add( p.getZ(), p.getY(), -p.getX());
            case SOUTH: return startPos.add(-p.getX(), p.getY(), -p.getZ());
            case WEST:  return startPos.add(-p.getZ(), p.getY(),  p.getX());
            default:    return startPos.add( p.getX(), p.getY(),  p.getZ());
        }
    }

    public Setting<Integer> getDelaySetting() {
        return delay;
    }
}
