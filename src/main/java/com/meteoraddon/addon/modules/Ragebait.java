package com.meteoraddon.addon.modules;

import com.meteoraddon.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

public class Ragebait extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public enum ToxicityLevel {
        MILD("Mild"),
        MODERATE("Moderate"),
        EXTREME("Extreme");

        private final String title;

        ToxicityLevel(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    private final Setting<ToxicityLevel> toxicityLevel = sgGeneral.add(new EnumSetting.Builder<ToxicityLevel>()
        .name("toxicity-level")
        .description("How toxic the generated messages should be.")
        .defaultValue(ToxicityLevel.MILD)
        .build()
    );

    private final Setting<Boolean> randomToxicity = sgGeneral.add(new BoolSetting.Builder()
        .name("random-toxicity")
        .description("Randomly vary toxicity level.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> addTarget = sgGeneral.add(new BoolSetting.Builder()
        .name("add-target")
        .description("Add random player names as targets.")
        .defaultValue(true)
        .build()
    );

    private final Setting<List<String>> customTargets = sgGeneral.add(new StringListSetting.Builder()
        .name("custom-targets")
        .description("Custom names to target.")
        .defaultValue(Arrays.asList("Steve", "Alex", "Noob", "Newbie"))
        .build()
    );

    private final Setting<Boolean> prefixMessages = sgGeneral.add(new BoolSetting.Builder()
        .name("prefix-messages")
        .description("Add toxic prefixes to messages.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> useApiInsults = sgGeneral.add(new BoolSetting.Builder()
        .name("use-api-insults")
        .description("Use insults from evilinsult.com API instead of hardcoded ones.")
        .defaultValue(false)
        .build()
    );

    private final Random random = new Random();

    private final List<String> mildInsults = Arrays.asList(
        "lol you're bad", "get good scrub", "ez game", "try harder", "lame",
        "noob move", "that was sad", "you suck", "git gud", "play better",
        "skill issue", "trash player", "you're awful", "step up", "pathetic",
        "weak", "loser", "terrible", "horrible", "bad at this"
    );

    private final List<String> moderateInsults = Arrays.asList(
        "you're actually trash", "uninstall the game", "how are you this bad",
        "worst player ever", "go back to tutorial", "you're a disgrace",
        "my grandma plays better", "delete your account", "you're useless",
        "just quit already", "you're a walking disappointment", "skill zero",
        "absolute garbage", "go touch grass", "you're unplayable", "embarrassing",
        "failure", "waste of time", "not worth it", "sad existence"
    );

    private final List<String> extremeInsults = Arrays.asList(
        "you're the definition of garbage", "your existence is a mistake",
        "I hope you never play this game again", "you're a waste of oxygen",
        "your parents are disappointed", "you should be banned for being this bad",
        "you're everything wrong with gaming", "kill your internet connection",
        "you're a stain on the community", "do everyone a favor and quit forever",
        "your birth was a tragedy", "you're a cancer to society", "die in a fire",
        "you're subhuman", "unworthy of life", "pure evil", "soul crushing loser",
        "end yourself", "you're a mistake", "obliterate your existence"
    );

    private final List<String> prefixes = Arrays.asList(
        "LMAO", "ROFL", "OMG", "BRUH", "DUDE", "YIKES", "CRINGE", "PATHETIC",
        "LOL", "HAHA", "KEKW", "WTF", "SMH", "FFS", "IDK", "TBH"
    );

    private final List<String> suffixes = Arrays.asList(
        "get rekt", "ez", "gg ez", "stay mad", "cry more", "seething", "triggered",
        "owned", "pwned", "ratio", "L", "skill diff", "cope harder", "malder",
        "take L", "betrayed", "exposed", "wrecked", "destroyed"
    );

    public Ragebait() {
        super(AddonTemplate.CATEGORY, "ragebait", "Generates toxic messages to bait reactions.");
    }

    @EventHandler
    private void onSendMessage(SendMessageEvent event) {
        String message = event.message;
        String toxicMessage = generateToxicMessage(message);
        
        if (!message.equals(toxicMessage)) {
            event.message = toxicMessage;
        }
    }

    private String generateToxicMessage(String original) {
        ToxicityLevel level = randomToxicity.get() ? 
            ToxicityLevel.values()[random.nextInt(ToxicityLevel.values().length)] : 
            toxicityLevel.get();

        String insult;
        if (useApiInsults.get()) {
            insult = fetchInsultFromApi();
        } else {
            List<String> insults = getInsultsForLevel(level);
            insult = insults.get(random.nextInt(insults.size()));
        }
        
        StringBuilder result = new StringBuilder();

        // Add prefix
        if (prefixMessages.get() && random.nextBoolean()) {
            result.append(prefixes.get(random.nextInt(prefixes.size()))).append(" ");
        }

        // Add target
        if (addTarget.get() && random.nextBoolean()) {
            String target = getRandomTarget();
            result.append(target).append(", ");
        }

        // Add main insult
        result.append(insult);

        // Add suffix
        if (random.nextBoolean()) {
            result.append(" ").append(suffixes.get(random.nextInt(suffixes.size())));
        }

        // Add original message if it exists and isn't just a command
        if (original != null && !original.startsWith("/") && random.nextBoolean()) {
            result.append(" | ").append(original);
        }

        return result.toString();
    }

    private List<String> getInsultsForLevel(ToxicityLevel level) {
        switch (level) {
            case MILD: return mildInsults;
            case MODERATE: return moderateInsults;
            case EXTREME: return extremeInsults;
            default: return mildInsults;
        }
    }

    private String getRandomTarget() {
        if (customTargets.get().isEmpty()) {
            return "player";
        }
        return customTargets.get().get(random.nextInt(customTargets.get().size()));
    }

    private String fetchInsultFromApi() {
        try {
            URL url = new URI("https://evilinsult.com/generate_insult.php?lang=en&type=plain%20text").toURL();
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // 5 seconds
            connection.setReadTimeout(5000); // 5 seconds

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                connection.disconnect();
                return content.toString();
            } else {
                warning("Failed to fetch insult from API. Response code: " + responseCode);
            }
        } catch (IOException | URISyntaxException e) {
            error("Error fetching insult from API: " + e.getMessage());
        }
        return "You are a smelly pirate!"; // Fallback insult
    }
}
