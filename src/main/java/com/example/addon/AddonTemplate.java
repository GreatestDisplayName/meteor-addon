package com.example.addon;

import com.example.addon.commands.ChatCommands;
import com.example.addon.commands.ChatCopyCommand;
import com.example.addon.commands.ChatListCommand;
import com.example.addon.commands.ChatSpamCommand;
import com.example.addon.commands.DebugCommand;
import com.example.addon.commands.WitherCommand;
import com.example.addon.commands.AutoStaircaseCommand;
import com.example.addon.modules.AntiBookBan;
import com.example.addon.modules.AutoReply;
import com.example.addon.modules.AutoStaircase;
import com.example.addon.modules.AutoWitherBuilder;
import com.example.addon.modules.AutoPyramid;
import com.example.addon.modules.ChatLogger;
import com.example.addon.modules.ChatMacro;
import com.example.addon.modules.ChatTextSelector;
import com.example.addon.modules.Insulit;
import com.example.addon.modules.IronGolemBuilder;
import com.example.addon.modules.SnowGolemBuilder;
import com.example.addon.modules.FluidPlacer;
import com.example.addon.modules.LavaBucketPvP;
import com.example.addon.modules.TNTPvP;
import com.example.addon.modules.LavaCast;
import com.example.addon.modules.LeetSpeak;
import com.example.addon.modules.MessageHighlighter;
import com.example.addon.modules.ModuleDisabler;
import com.example.addon.modules.Ragebait;
import com.example.addon.modules.SpamPlus;
import com.example.addon.modules.WorldOriginMarker;
import com.example.addon.hud.ChatTextHud;
import com.example.addon.hud.LavaHud;
import com.example.addon.hud.ModuleStatusHud;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public class AddonTemplate extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger("MeteorAddon");
    public static final Category CATEGORY = new Category("Example");
    public static final HudGroup HUD_GROUP = new HudGroup("Example");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Meteor Addon Template");

        // Modules
        Modules.get().add(new WorldOriginMarker());
        Modules.get().add(new AutoWitherBuilder());
        Modules.get().add(new AutoPyramid());
        Modules.get().add(new AutoStaircase());
        Modules.get().add(new LavaCast());
        Modules.get().add(new FluidPlacer());
        Modules.get().add(new LavaBucketPvP());
        Modules.get().add(new TNTPvP());
        Modules.get().add(new LeetSpeak());
        Modules.get().add(new Insulit());
        Modules.get().add(new Ragebait());
        Modules.get().add(new ChatLogger());
        Modules.get().add(new AutoReply());
        Modules.get().add(new MessageHighlighter());
        Modules.get().add(new ChatMacro());
        Modules.get().add(new SpamPlus());
        Modules.get().add(new ModuleDisabler());
        Modules.get().add(new AntiBookBan());
        Modules.get().add(new SnowGolemBuilder());
        Modules.get().add(new IronGolemBuilder());

        // Commands
        Commands.add(new ChatCommands());
        Commands.add(new WitherCommand());
        Commands.add(new ChatCopyCommand());
        Commands.add(new ChatListCommand());
        Commands.add(new ChatSpamCommand());
        Commands.add(new DebugCommand());
        Commands.add(new AutoStaircaseCommand());

        // HUD
        Hud.get().register(ModuleStatusHud.INFO);
        Hud.get().register(ChatTextHud.INFO);
        Hud.get().register(LavaHud.INFO);
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "com.example.addon";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("MeteorDevelopment", "meteor-addon-template");
    }
}
