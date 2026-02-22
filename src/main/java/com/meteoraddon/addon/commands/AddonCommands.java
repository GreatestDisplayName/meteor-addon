package com.meteoraddon.addon.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class AddonCommands extends Command {
    public AddonCommands() {
        super("addon", "A command for addon-specific utilities.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {

        builder.then(literal("info").executes(context -> {
            info("Version 1.0 of the addon.");
            return SINGLE_SUCCESS;
        }));
    }
}
