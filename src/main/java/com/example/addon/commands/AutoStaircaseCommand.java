package com.example.addon.commands;

import com.example.addon.modules.AutoStaircase;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class AutoStaircaseCommand extends Command {
    public AutoStaircaseCommand() {
        super("autostaircase", "Controls the AutoStaircase module.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("toggle").executes(context -> {
            AutoStaircase module = Modules.get().get(AutoStaircase.class);
            if (module != null) {
                module.toggle();
                ChatUtils.info("AutoStaircase " + (module.isActive() ? "enabled" : "disabled"));
            } else {
                ChatUtils.error("AutoStaircase module not found.");
            }
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("height").then(argument("value", IntegerArgumentType.integer(1, 100)).executes(context -> {
            int height = IntegerArgumentType.getInteger(context, "value");
            AutoStaircase module = Modules.get().get(AutoStaircase.class);
            if (module != null) {
                module.height.set(height);
                ChatUtils.info("AutoStaircase height set to " + height);
            } else {
                ChatUtils.error("AutoStaircase module not found.");
            }
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("help").executes(context -> {
            ChatUtils.info("AutoStaircase commands:");
            ChatUtils.info("  toggle - Toggles the module on/off");
            ChatUtils.info("  height <value> - Sets the staircase height (1-100)");
            ChatUtils.info("  help - Shows this help message");
            return SINGLE_SUCCESS;
        }));
    }
}
