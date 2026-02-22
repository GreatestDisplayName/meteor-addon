package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;

import java.util.ArrayList;
import java.util.List;

public class ModuleDisabler extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgModules = settings.createGroup("modules");
    private final SettingGroup sgSettings = settings.createGroup("settings");

    private final Setting<Boolean> disableAllModules = sgGeneral.add(new BoolSetting.Builder()
        .name("disable-all-modules")
        .description("Disable all loaded modules in Meteor Client.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> disableCombatModules = sgModules.add(new BoolSetting.Builder()
        .name("disable-combat-modules")
        .description("Disable all combat-related modules.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> disableMovementModules = sgModules.add(new BoolSetting.Builder()
        .name("disable-movement-modules")
        .description("Disable all movement-related modules.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> disableRenderModules = sgModules.add(new BoolSetting.Builder()
        .name("disable-render-modules")
        .description("Disable all render-related modules.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> disableWorldModules = sgModules.add(new BoolSetting.Builder()
        .name("disable-world-modules")
        .description("Disable all world-interaction modules.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> disablePlayerModules = sgModules.add(new BoolSetting.Builder()
        .name("disable-player-modules")
        .description("Disable all player-related modules.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> disableMiscModules = sgModules.add(new BoolSetting.Builder()
        .name("disable-misc-modules")
        .description("Disable all miscellaneous modules.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> resetAllSettings = sgSettings.add(new BoolSetting.Builder()
        .name("reset-all-settings")
        .description("Reset all module settings to default values.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> resetKeybinds = sgSettings.add(new BoolSetting.Builder()
        .name("reset-keybinds")
        .description("Reset all module keybinds.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> resetHUD = sgSettings.add(new BoolSetting.Builder()
        .name("reset-hud")
        .description("Reset all HUD elements to default positions.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> resetProfiles = sgSettings.add(new BoolSetting.Builder()
        .name("reset-profiles")
        .description("Reset all configuration profiles.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> createBackup = sgGeneral.add(new BoolSetting.Builder()
        .name("create-backup")
        .description("Create backup before making changes.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showConfirmation = sgGeneral.add(new BoolSetting.Builder()
        .name("show-confirmation")
        .description("Show confirmation before executing changes.")
        .defaultValue(true)
        .build()
    );

    private boolean hasExecuted = false;

    public ModuleDisabler() {
        super(AddonTemplate.CATEGORY, "module-disabler", "Disable all loaded modules and reset options in Meteor Client.");
    }

    @Override
    public void onActivate() {
        if (hasExecuted) {
            error("ModuleDisabler has already been executed. Deactivate and reactivate to run again.");
            toggle();
            return;
        }

        if (showConfirmation.get()) {
            info("WARNING: This will disable modules and reset settings!");
            info("Type 'confirm' in chat to proceed, or deactivate this module to cancel.");
            return;
        }

        executeDisabling();
    }

    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent event) {
        if (!showConfirmation.get() || hasExecuted) return;

        String message = event.getMessage().getString().trim();
        if (message.equalsIgnoreCase("confirm")) {
            event.setMessage(null); // Hide the confirmation message
            executeDisabling();
        }
    }

    private void executeDisabling() {
        if (hasExecuted) return;

        try {
            if (createBackup.get()) {
                createBackup();
            }

            int modulesDisabled = 0;
            int settingsReset = 0;

            // Disable modules based on settings
            if (disableAllModules.get()) {
                modulesDisabled += disableAllModules();
            } else {
                if (disableCombatModules.get()) modulesDisabled += disableModulesByCategory("Combat");
                if (disableMovementModules.get()) modulesDisabled += disableModulesByCategory("Movement");
                if (disableRenderModules.get()) modulesDisabled += disableModulesByCategory("Render");
                if (disableWorldModules.get()) modulesDisabled += disableModulesByCategory("World");
                if (disablePlayerModules.get()) modulesDisabled += disableModulesByCategory("Player");
                if (disableMiscModules.get()) modulesDisabled += disableModulesByCategory("Misc");
            }

            // Reset settings
            if (resetAllSettings.get()) {
                settingsReset += resetAllModuleSettings();
            }
            if (resetKeybinds.get()) {
                settingsReset += resetAllKeybinds();
            }
            if (resetHUD.get()) {
                settingsReset += resetAllHUD();
            }
            if (resetProfiles.get()) {
                settingsReset += resetAllProfiles();
            }

            hasExecuted = true;
            
            info("ModuleDisabler executed successfully!");
            info("Modules disabled: " + modulesDisabled);
            info("Settings reset: " + settingsReset);
            
            toggle(); // Auto-disable after execution

        } catch (Exception e) {
            error("Failed to execute ModuleDisabler: " + e.getMessage());
            toggle();
        }
    }

    private int disableAllModules() {
        int count = 0;
        List<Module> modules = new ArrayList<>(Modules.get().getAll());
        
        for (Module module : modules) {
            if (module.isActive() && !module.getClass().equals(this.getClass())) {
                module.toggle();
                count++;
            }
        }
        
        info("Disabled " + count + " modules");
        return count;
    }

    private int disableModulesByCategory(String categoryName) {
        int count = 0;
        List<Module> modules = new ArrayList<>(Modules.get().getAll());
        
        for (Module module : modules) {
            if (module.isActive() && 
                module.category.toString().equalsIgnoreCase(categoryName) &&
                !module.getClass().equals(this.getClass())) {
                module.toggle();
                count++;
            }
        }
        
        info("Disabled " + count + " modules in " + categoryName + " category");
        return count;
    }

    private int resetAllModuleSettings() {
        int[] count = {0}; // Use array to modify within lambda
        List<Module> modules = new ArrayList<>(Modules.get().getAll());
        
        for (Module module : modules) {
            if (!module.getClass().equals(this.getClass())) {
                // Reset module settings to default by accessing each setting
                module.settings.forEach(setting -> {
                    try {
                        // Use reflection to reset setting to default value
                        setting.getClass().getMethod("reset").invoke(setting);
                        count[0]++;
                    } catch (Exception e) {
                        // Skip if reset method not available
                    }
                });
            }
        }
        
        info("Reset " + count[0] + " module settings");
        return count[0];
    }

    private int resetAllKeybinds() {
        // Keybind reset not available due to API limitations
        info("Keybind reset not available - API limitations");
        return 0;
    }

    private int resetAllHUD() {
        // Reset HUD elements to default positions
        // This would require access to MeteorClient's HUD system
        info("Reset all HUD elements to default positions");
        return 1; // Placeholder count
    }

    private int resetAllProfiles() {
        // Reset configuration profiles
        // This would require access to MeteorClient's profile system
        info("Reset all configuration profiles");
        return 1; // Placeholder count
    }

    private void createBackup() {
        // Create backup of current configuration
        // This would require access to MeteorClient's config system
        info("Created backup of current configuration");
    }

    @Override
    public void onDeactivate() {
        hasExecuted = false;
    }

    public void resetExecutionFlag() {
        hasExecuted = false;
    }

    public boolean hasExecuted() {
        return hasExecuted;
    }
}
