package com.meteoraddon.addon.commands;

import com.meteoraddon.addon.modules.ChatTextSelector;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ChatListCommand extends Command {
    public ChatListCommand() {
        super("chatlist", "Lists chat message history.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            ChatTextSelector chatSelector = Modules.get().get(ChatTextSelector.class);
            
            if (chatSelector != null && chatSelector.isActive()) {
                chatSelector.listMessages();
            } else {
                error("ChatTextSelector module is not enabled!");
            }
            
            return SINGLE_SUCCESS;
        });
    }
}
