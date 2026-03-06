package com.meteoraddon.addon.commands;

import com.meteoraddon.addon.modules.Ragebait;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ChatCommands extends Command {
    public ChatCommands() {
        super("chat", "Manage Ragebait module. Use .chat ragebait");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        // --- Ragebait Subcommands ---
        builder.then(literal("ragebait")
            .then(literal("info").executes(context -> {
                var module = Modules.get().get(Ragebait.class);
                if (module == null) {
                    ChatUtils.sendMsg(Text.literal("Ragebait module not found!"));
                    return SINGLE_SUCCESS;
                }

                StringBuilder status = new StringBuilder();
                status.append("§6Ragebait Status:\n");
                status.append("§fEnabled: §a").append(module.isActive() ? "Yes" : "No").append("\n");

                ChatUtils.sendMsg(Text.literal(status.toString()));
                return SINGLE_SUCCESS;
            }))
            .then(literal("toggle").executes(context -> {
                var module = Modules.get().get(Ragebait.class);
                if (module == null) {
                    ChatUtils.sendMsg(Text.literal("Ragebait module not found!"));
                    return SINGLE_SUCCESS;
                }

                module.toggle();
                String state = module.isActive() ? "§aenabled" : "§cdisabled";
                ChatUtils.sendMsg(Text.literal("Ragebait " + state));
                return SINGLE_SUCCESS;
            }))
        );
    }
}