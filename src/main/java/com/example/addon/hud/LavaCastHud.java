package com.example.addon.hud;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class LavaCastHud extends HudElement {
    public static final HudElementInfo<LavaCastHud> INFO = new HudElementInfo<>(AddonTemplate.HUD_GROUP, "lava-hud", "Shows nearby lava sources and flowing lava.", LavaCastHud::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> radius = sgGeneral.add(new IntSetting.Builder()
        .name("radius")
        .description("Radius to search for lava blocks.")
        .defaultValue(10)
        .min(1)
        .max(50)
        .sliderMax(20)
        .build()
    );

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("color")
        .description("Color of the text.")
        .defaultValue(new SettingColor(255, 100, 0))
        .build()
    );

    public LavaCastHud() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        if (MeteorClient.mc.player == null || MeteorClient.mc.world == null) return;

        int sources = 0;
        int flowing = 0;

        BlockPos playerPos = MeteorClient.mc.player.getBlockPos();
        int r = radius.get();

        for (int x = playerPos.getX() - r; x <= playerPos.getX() + r; x++) {
            for (int y = playerPos.getY() - r; y <= playerPos.getY() + r; y++) {
                for (int z = playerPos.getZ() - r; z <= playerPos.getZ() + r; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (MeteorClient.mc.world.getBlockState(pos).isOf(Blocks.LAVA)) {
                        // Check if source (level 8) or flowing (level <8)
                        int level = MeteorClient.mc.world.getFluidState(pos).getLevel();
                        if (level == 8) {
                            sources++;
                        } else {
                            flowing++;
                        }
                    }
                }
            }
        }

        String text = "Lava - Sources: " + sources + ", Flowing: " + flowing;
        renderer.text(text, x, y, color.get(), true);
        setSize(renderer.textWidth(text), renderer.textHeight());
    }
}
