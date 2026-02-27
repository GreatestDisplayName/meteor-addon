package com.meteoraddon.addon.commands;

import com.meteoraddon.addon.modules.Ragebait;
import com.meteoraddon.addon.modules.Ragebait.ToxicityLevel;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ChatCommands extends Command {
    public ChatCommands() {
        super("chat", "Manage Ragebait module. Use .chat ragebait");
    }

    @SuppressWarnings("unchecked")
    private <T> T getField(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(obj);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> getStringListField(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(obj);
            if (value instanceof List) {
                return (List<String>) value;
            }
            return new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private <T> void setField(Object obj, String fieldName, T value) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            // Silently fail
        }
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
                status.append("§fToxicity Level: §a").append(getField(module, "toxicityLevel") != null ? getField(module, "toxicityLevel").toString() : "Unknown").append("\n");
                status.append("§fRandom Toxicity: §a").append(getField(module, "randomToxicity") != null ? getField(module, "randomToxicity").toString().contains("true") ? "Yes" : "No" : "Unknown").append("\n");
                status.append("§fAdd Target: §a").append(getField(module, "addTarget") != null ? getField(module, "addTarget").toString().contains("true") ? "Yes" : "No" : "Unknown").append("\n");
                status.append("§fCustom Targets: §a").append(getField(module, "customTargets") != null ? getField(module, "customTargets").toString() : "None").append("\n");
                status.append("§fPrefix Messages: §a").append(getField(module, "prefixMessages") != null ? getField(module, "prefixMessages").toString().contains("true") ? "Yes" : "No" : "Unknown").append("\n");

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
            .then(literal("set-toxicity")
                .then(literal("low").executes(context -> {
                    var module = Modules.get().get(Ragebait.class);
                    if (module == null) {
                        ChatUtils.sendMsg(Text.literal("Ragebait module not found!"));
                        return SINGLE_SUCCESS;
                    }
                    setField(module, "toxicityLevel", "LOW");
                    ChatUtils.sendMsg(Text.literal("Ragebait toxicity level set to §aLOW§f."));
                    return SINGLE_SUCCESS;
                }))
                .then(literal("medium").executes(context -> {
                    var module = Modules.get().get(Ragebait.class);
                    if (module == null) {
                        ChatUtils.sendMsg(Text.literal("Ragebait module not found!"));
                        return SINGLE_SUCCESS;
                    }
                    setField(module, "toxicityLevel", "MEDIUM");
                    ChatUtils.sendMsg(Text.literal("Ragebait toxicity level set to §aMEDIUM§f."));
                    return SINGLE_SUCCESS;
                }))
                .then(literal("high").executes(context -> {
                    var module = Modules.get().get(Ragebait.class);
                    if (module == null) {
                        ChatUtils.sendMsg(Text.literal("Ragebait module not found!"));
                        return SINGLE_SUCCESS;
                    }
                    setField(module, "toxicityLevel", "HIGH");
                    ChatUtils.sendMsg(Text.literal("Ragebait toxicity level set to §aHIGH§f."));
                    return SINGLE_SUCCESS;
                }))
            )
            .then(literal("set-random-toxicity")
                .then(argument("value", BoolArgumentType.bool()).executes(context -> {
                    var module = Modules.get().get(Ragebait.class);
                    if (module == null) {
                        ChatUtils.sendMsg(Text.literal("Ragebait module not found!"));
                        return SINGLE_SUCCESS;
                    }
                    boolean value = context.getArgument("value", Boolean.class);
                    setField(module, "randomToxicity", value);
                    ChatUtils.sendMsg(Text.literal("Ragebait random toxicity set to §a" + (value ? "enabled" : "disabled") + "§f."));
                    return SINGLE_SUCCESS;
                }))
            )
            .then(literal("set-add-target")
                .then(argument("value", BoolArgumentType.bool()).executes(context -> {
                    var module = Modules.get().get(Ragebait.class);
                    if (module == null) {
                        ChatUtils.sendMsg(Text.literal("Ragebait module not found!"));
                        return SINGLE_SUCCESS;
                    }
                    boolean value = context.getArgument("value", Boolean.class);
                    setField(module, "addTarget", value);
                    ChatUtils.sendMsg(Text.literal("Ragebait add target set to §a" + (value ? "enabled" : "disabled") + "§f."));
                    return SINGLE_SUCCESS;
                }))
            )
            .then(literal("set-prefix-messages")
                .then(argument("value", BoolArgumentType.bool()).executes(context -> {
                    var module = Modules.get().get(Ragebait.class);
                    if (module == null) {
                        ChatUtils.sendMsg(Text.literal("Ragebait module not found!"));
                        return SINGLE_SUCCESS;
                    }
                    boolean value = context.getArgument("value", Boolean.class);
                    setField(module, "prefixMessages", value);
                    ChatUtils.sendMsg(Text.literal("Ragebait prefix messages set to §a" + (value ? "enabled" : "disabled") + "§f."));
                    return SINGLE_SUCCESS;
                }))
            )
            .then(literal("custom-targets")
                .then(literal("add")
                    .then(argument("name", StringArgumentType.greedyString()).executes(context -> {
                        var module = Modules.get().get(Ragebait.class);
                        if (module == null) {
                            ChatUtils.sendMsg(Text.literal("Ragebait module not found!"));
                            return SINGLE_SUCCESS;
                        }
                        String name = context.getArgument("name", String.class);
                        List<String> targets = new ArrayList<>(getStringListField(module, "customTargets"));
                        if (!targets.contains(name)) {
                            targets.add(name);
                            setField(module, "customTargets", targets);
                            ChatUtils.sendMsg(Text.literal("§aAdded custom target: §f" + name));
                        } else {
                            ChatUtils.sendMsg(Text.literal("§eCustom target '" + name + "' already exists."));
                        }
                        return SINGLE_SUCCESS;
                    }))
                )
                .then(literal("remove")
                    .then(argument("name", StringArgumentType.greedyString()).executes(context -> {
                        var module = Modules.get().get(Ragebait.class);
                        if (module == null) {
                            ChatUtils.sendMsg(Text.literal("Ragebait module not found!"));
                            return SINGLE_SUCCESS;
                        }
                        String name = context.getArgument("name", String.class);
                        List<String> targets = new ArrayList<>(getStringListField(module, "customTargets"));
                        if (targets.remove(name)) {
                            setField(module, "customTargets", targets);
                            ChatUtils.sendMsg(Text.literal("§aRemoved custom target: §f" + name));
                        } else {
                            ChatUtils.sendMsg(Text.literal("§eCustom target '" + name + "' not found."));
                        }
                        return SINGLE_SUCCESS;
                    }))
                )
                .then(literal("list").executes(context -> {
                    var module = Modules.get().get(Ragebait.class);
                    if (module == null) {
                        ChatUtils.sendMsg(Text.literal("Ragebait module not found!"));
                        return SINGLE_SUCCESS;
                    }
                    List<String> targets = getStringListField(module, "customTargets");
                    if (targets.isEmpty()) {
                        ChatUtils.sendMsg(Text.literal("§fNo custom targets set."));
                    } else {
                        ChatUtils.sendMsg(Text.literal("§fCustom targets: §a" + String.join(", ", targets)));
                    }
                    return SINGLE_SUCCESS;
                }))
            )
        );
    }
}