package com.meteoraddon.addon;

import com.meteoraddon.addon.commands.ChatCommands;
import com.meteoraddon.addon.commands.ChatCopyCommand;
import com.meteoraddon.addon.commands.ChatListCommand;
import com.meteoraddon.addon.commands.ChatSpamCommand;
import com.meteoraddon.addon.commands.DebugCommand;
import com.meteoraddon.addon.commands.WitherCommand;
import com.meteoraddon.addon.commands.AutoStaircaseCommand;
import com.meteoraddon.addon.commands.KaboomCommands;
import com.meteoraddon.addon.modules.AntiBookBan;
import com.meteoraddon.addon.modules.AutoReply;
import com.meteoraddon.addon.modules.AutoStaircase;
import com.meteoraddon.addon.modules.AutoWitherBuilder;
import com.meteoraddon.addon.modules.AutoPyramid;
import com.meteoraddon.addon.modules.ChatLogger;
import com.meteoraddon.addon.modules.ChatMacro;
import com.meteoraddon.addon.modules.ChatSpamFilter;
import com.meteoraddon.addon.modules.ChatTextSelector;
import com.meteoraddon.addon.modules.FluidPlacer;
import com.meteoraddon.addon.modules.IronGolemBuilder;
import com.meteoraddon.addon.modules.KaboomChatFiller;
import com.meteoraddon.addon.modules.SnowGolemBuilder;
import com.meteoraddon.addon.modules.LavaBucketPvP;
import com.meteoraddon.addon.modules.TNTPvP;
import com.meteoraddon.addon.modules.LavaCast;
import com.meteoraddon.addon.modules.LeetSpeak;
import com.meteoraddon.addon.modules.MessageHighlighter;
import com.meteoraddon.addon.modules.ModuleDisabler;
import com.meteoraddon.addon.modules.Ragebait;
import com.meteoraddon.addon.modules.SpamPlus;
import com.meteoraddon.addon.modules.WorldOriginMarker;
import com.meteoraddon.addon.modules.SignPlus;
import com.meteoraddon.addon.modules.SpeedHaxPlus;
import com.meteoraddon.addon.modules.MineNotStone;
import com.meteoraddon.addon.modules.BucketSequence;
import com.meteoraddon.addon.modules.Ragebait;
import com.meteoraddon.addon.modules.ModuleDisabler;
import com.meteoraddon.addon.modules.SpamPlus;
import com.meteoraddon.addon.modules.WorldOriginMarker;
import com.meteoraddon.addon.modules.SgaTranslator;
import com.meteoraddon.addon.modules.TpSpammer;
import com.meteoraddon.addon.modules.ProxyScraper;
import com.meteoraddon.addon.modules.ProxyManager;
import com.meteoraddon.addon.modules.TpKillAura;
import com.meteoraddon.addon.modules.RanbooEndermanLang;
import com.meteoraddon.addon.modules.EncryptMessage;
import com.meteoraddon.addon.hud.ChatTextHud;
import com.meteoraddon.addon.hud.LavaHud;
import com.meteoraddon.addon.hud.ModuleStatusHud;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import java.net.Proxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public class AddonTemplate extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger("MeteorAddon");
    public static final Category CATEGORY = new Category("Example");
    public static final HudGroup HUD_GROUP = new HudGroup("Example");
    public static Proxy currentProxy = null;

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
        Modules.get().add(new ChatSpamFilter());
        Modules.get().add(new KaboomChatFiller());
        Modules.get().add(new SpeedHaxPlus());
        Modules.get().add(new MineNotStone());
        Modules.get().add(new SignPlus());
        Modules.get().add(new BucketSequence());
        Modules.get().add(new SgaTranslator());
        Modules.get().add(new TpSpammer());
        Modules.get().add(new ProxyScraper());
        Modules.get().add(new ProxyManager());
        Modules.get().add(new TpKillAura());
        Modules.get().add(new RanbooEndermanLang());
        Modules.get().add(new EncryptMessage());

        // Commands
        Commands.add(new ChatCommands());
        Commands.add(new WitherCommand());
        Commands.add(new ChatCopyCommand());
        Commands.add(new ChatListCommand());
        Commands.add(new ChatSpamCommand());
        Commands.add(new DebugCommand());
        Commands.add(new AutoStaircaseCommand());
        Commands.add(new KaboomCommands());

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
        return "com.meteoraddon.addon";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("MeteorDevelopment", "meteor-addon-template");
    }
}
