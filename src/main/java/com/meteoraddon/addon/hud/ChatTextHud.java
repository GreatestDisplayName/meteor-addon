package com.meteoraddon.addon.hud;

import com.meteoraddon.addon.AddonTemplate;
import com.meteoraddon.addon.modules.ChatTextSelector;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public class ChatTextHud extends HudElement {
    public static final HudElementInfo<ChatTextHud> INFO = new HudElementInfo<>(AddonTemplate.HUD_GROUP, "chat-text-hud", "Displays the status of the Chat Text Selector module.", ChatTextHud::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<SettingColor> textColor = sgGeneral.add(new ColorSetting.Builder()
        .name("text-color")
        .description("Color for the status text.")
        .defaultValue(new SettingColor(255, 255, 255))
        .build()
    );

    public ChatTextHud() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        String text = "Chat Text Selector is active.";
        if (!Modules.get().get(ChatTextSelector.class).isActive()) {
            text = "Chat Text Selector is inactive.";
        }
        setSize(renderer.textWidth(text, true), renderer.textHeight(true));
        renderer.text(text, x, y, textColor.get(), true);
    }
}
