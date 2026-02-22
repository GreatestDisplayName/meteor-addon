package com.meteoraddon.addon.hud;

import com.meteoraddon.addon.AddonTemplate;
import com.meteoraddon.addon.modules.AutoWitherBuilder;
import com.meteoraddon.addon.modules.WorldOriginMarker;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public class ModuleStatusHud extends HudElement {
    public static final HudElementInfo<ModuleStatusHud> INFO = new HudElementInfo<>(AddonTemplate.HUD_GROUP, "module-status", "Shows the status of custom addon modules.", ModuleStatusHud::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> showActiveOnly = sgGeneral.add(new BoolSetting.Builder()
        .name("show-active-only")
        .description("Only shows modules that are currently active.")
        .defaultValue(false)
        .build()
    );

    private final Setting<SettingColor> activeColor = sgGeneral.add(new ColorSetting.Builder()
        .name("active-color")
        .description("The color for active modules.")
        .defaultValue(new SettingColor(0, 255, 0))
        .build()
    );

    private final Setting<SettingColor> inactiveColor = sgGeneral.add(new ColorSetting.Builder()
        .name("inactive-color")
        .description("The color for inactive modules.")
        .defaultValue(new SettingColor(255, 0, 0))
        .build()
    );

    public ModuleStatusHud() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        double width = 0;
        double height = 0;

        AutoWitherBuilder witherBuilder = Modules.get().get(AutoWitherBuilder.class);
        WorldOriginMarker originMarker = Modules.get().get(WorldOriginMarker.class);

        double w = drawModuleStatus(renderer, "Wither Builder", witherBuilder.isActive(), x, y + height);
        if (w > 0) {
            width = Math.max(width, w);
            height += renderer.textHeight();
        }

        w = drawModuleStatus(renderer, "Origin Marker", originMarker.isActive(), x, y + height);
        if (w > 0) {
            width = Math.max(width, w);
            height += renderer.textHeight();
        }

        setSize(width, height);
    }

    private double drawModuleStatus(HudRenderer renderer, String name, boolean active, double x, double y) {
        if (showActiveOnly.get() && !active) return 0;
        
        SettingColor color = active ? activeColor.get() : inactiveColor.get();
        renderer.text(name, x, y, color, true);
        return renderer.textWidth(name);
    }
}
