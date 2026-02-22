package com.example.addon.commands;

import com.example.addon.utils.TextDebug;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;

public class DebugCommand extends Command {
    public DebugCommand() {
        super("debug", "Controls text debug logging for the addon");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            TextDebug.printStatus();
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        });
        
        builder.then(literal("status").executes(context -> {
            TextDebug.printStatus();
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        }));
        
        builder.then(literal("file").executes(context -> {
            TextDebug.setFileLogging(!TextDebug.isFileLoggingEnabled());
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        }));
        
        builder.then(literal("chat").executes(context -> {
            TextDebug.setChatLogging(!TextDebug.isChatLoggingEnabled());
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        }));
        
        builder.then(literal("console").executes(context -> {
            TextDebug.setConsoleLogging(!TextDebug.isConsoleLoggingEnabled());
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        }));
        
        builder.then(literal("clear").executes(context -> {
            TextDebug.clearLogFile();
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        }));
        
        builder.then(literal("test").executes(context -> {
            TextDebug.info("This is an info message");
            TextDebug.warn("This is a warning message");
            TextDebug.error("This is an error message");
            TextDebug.debug("This is a debug message");
            TextDebug.trace("This is a trace message");
            
            TextDebug.module("TestModule", "Module test message");
            TextDebug.command("testcmd", "Command test message");
            TextDebug.hud("TestHud", "HUD test message");
            TextDebug.event("TestEvent", "Event test message");
            
            TextDebug.position("Test", 100, 64, 200);
            TextDebug.blockState("Test", "minecraft:stone", 100, 64, 200);
            TextDebug.inventory("Test", "minecraft:dirt", 5, 64);
            
            long start = System.currentTimeMillis();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            TextDebug.timing("TestOperation", start);
            
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        }));
    }
}
