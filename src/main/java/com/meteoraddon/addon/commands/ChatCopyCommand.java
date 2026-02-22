package com.meteoraddon.addon.commands;

import com.meteoraddon.addon.modules.ChatTextSelector;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ChatCopyCommand extends Command {
    public ChatCopyCommand() {
        super("chatcopy", "Copies chat messages to clipboard.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        // .chatcopy - copies last message
        builder.executes(context -> {
            ChatTextSelector chatSelector = Modules.get().get(ChatTextSelector.class);
            
            if (chatSelector != null && chatSelector.isActive()) {
                chatSelector.copyLastMessage();
            } else {
                error("ChatTextSelector module is not enabled!");
            }
            
            return SINGLE_SUCCESS;
        });

        // .chatcopy <number> - copies specific message
        builder.then(argument("number", IntegerArgumentType.integer(1, 10))
            .executes(context -> {
                int messageNumber = IntegerArgumentType.getInteger(context, "number");
                ChatTextSelector chatSelector = Modules.get().get(ChatTextSelector.class);
                
                if (chatSelector != null && chatSelector.isActive()) {
                    chatSelector.copyMessage(messageNumber - 1); // Convert to 0-based index
                } else {
                    error("ChatTextSelector module is not enabled!");
                }
                
                return SINGLE_SUCCESS;
            })
        );
    }
}
