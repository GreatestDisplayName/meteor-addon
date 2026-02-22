package com.meteoraddon.addon.commands;

import com.meteoraddon.addon.modules.AutoWitherBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class WitherCommand extends Command {
    public WitherCommand() {
        super("wither", "Toggles the AutoWitherBuilder module.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            Modules.get().get(AutoWitherBuilder.class).toggle();
            return SINGLE_SUCCESS;
        });
    }
}
