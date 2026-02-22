package com.meteoraddon.addon.modules;

import com.meteoraddon.addon.AddonTemplate;
import com.meteoraddon.addon.utils.TextDebug;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Box;

public class WorldOriginMarker extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgRender = this.settings.createGroup("Render");

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("The scale of the marker.")
        .defaultValue(1.0)
        .min(0.1)
        .sliderRange(0.1, 5.0)
        .build()
    );

    private final Setting<SettingColor> color = sgRender.add(new ColorSetting.Builder()
        .name("color")
        .description("The color of the marker.")
        .defaultValue(new SettingColor(255, 0, 255))
        .build()
    );

    public WorldOriginMarker() {
        super(AddonTemplate.CATEGORY, "world-origin-marker", "Highlights the world's origin (0,0,0).");
    }

    @Override
    public void onActivate() {
        TextDebug.module("WorldOriginMarker", "Activated with scale: %.2f", scale.get());
        TextDebug.module("WorldOriginMarker", "Marker color: RGB(%d,%d,%d)", 
            color.get().r, color.get().g, color.get().b);
    }

    @Override
    public void onDeactivate() {
        TextDebug.module("WorldOriginMarker", "Deactivated");
    }

    @EventHandler
    private void onRender3d(Render3DEvent event) {
        double s = scale.get() / 2.0;
        event.renderer.box(new Box(-s, -s, -s, s, s, s), color.get(), color.get(), ShapeMode.Both, 0);
        
        // Debug rendering every 60 ticks (approximately 3 seconds)
        if (mc.world.getTime() % 60 == 0) {
            TextDebug.trace("WorldOriginMarker", "Rendering origin box at scale %.2f", scale.get());
        }
    }
}
