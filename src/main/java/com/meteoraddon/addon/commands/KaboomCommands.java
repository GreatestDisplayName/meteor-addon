package com.meteoraddon.addon.commands;

import com.meteoraddon.addon.modules.KaboomChatFiller;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class KaboomCommands extends Command {
    public KaboomCommands() {
        super("kaboom", "Commands for Kaboom Chat Filler.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {

        builder.then(literal("toggle").executes(context -> {
            Modules.get().get(KaboomChatFiller.class).toggle();
            info("Toggled Kaboom Chat Filler");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("status").executes(context -> {
            boolean active = Modules.get().get(KaboomChatFiller.class).isActive();
            info("Kaboom Chat Filler is " + (active ? "active" : "inactive"));
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("info").executes(context -> {
            info("Kaboom Chat Filler filters spam from kaboom.pw chat.");
            return SINGLE_SUCCESS;
        }));
    }
}
