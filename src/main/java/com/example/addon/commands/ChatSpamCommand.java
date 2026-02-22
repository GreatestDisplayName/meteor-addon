package com.example.addon.commands;

import com.example.addon.modules.ChatTextSelector;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ChatSpamCommand extends Command {
    public ChatSpamCommand() {
        super("chatspam", "Manages anti-spam settings and stats.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        // .chatspam clear - clears spam data
        builder.then(literal("clear")
            .executes(context -> {
                ChatTextSelector chatSelector = Modules.get().get(ChatTextSelector.class);
                
                if (chatSelector != null && chatSelector.isActive()) {
                    chatSelector.clearSpamData();
                } else {
                    error("ChatTextSelector module is not enabled!");
                }
                
                return SINGLE_SUCCESS;
            })
        );

        // .chatspam stats - shows spam statistics
        builder.then(literal("stats")
            .executes(context -> {
                ChatTextSelector chatSelector = Modules.get().get(ChatTextSelector.class);
                
                if (chatSelector != null && chatSelector.isActive()) {
                    chatSelector.showSpamStats();
                } else {
                    error("ChatTextSelector module is not enabled!");
                }
                
                return SINGLE_SUCCESS;
            })
        );

        // .chatspam status - shows exploit protection status
        builder.then(literal("status")
            .executes(context -> {
                ChatTextSelector chatSelector = Modules.get().get(ChatTextSelector.class);
                
                if (chatSelector != null && chatSelector.isActive()) {
                    chatSelector.showExploitStatus();
                } else {
                    error("ChatTextSelector module is not enabled!");
                }
                
                return SINGLE_SUCCESS;
            })
        );
    }
}
