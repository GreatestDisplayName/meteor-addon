package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import javax.net.ssl.HttpsURLConnection;

public class Insulit extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> filterIncoming = sgGeneral.add(new BoolSetting.Builder()
        .name("filter-incoming")
        .description("Filter insults from incoming chat messages.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> filterOutgoing = sgGeneral.add(new BoolSetting.Builder()
        .name("filter-outgoing")
        .description("Filter insults from your outgoing messages.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> replaceWithStars = sgGeneral.add(new BoolSetting.Builder()
        .name("replace-with-stars")
        .description("Replace insults with asterisks instead of removing them.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> replaceWithApiInsult = sgGeneral.add(new BoolSetting.Builder()
        .name("replace-with-api-insult")
        .description("Replace filtered insults with a random insult from the evilinsult.com API.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> customWords = sgGeneral.add(new BoolSetting.Builder()
        .name("custom-words")
        .description("Enable custom word filtering.")
        .defaultValue(true)
        .build()
    );

    private final Setting<List<String>> customInsults = sgGeneral.add(new StringListSetting.Builder()
        .name("custom-insults")
        .description("Custom words to filter.")
        .defaultValue(Arrays.asList("idiot", "stupid", "dumb", "moron", "fool"))
        .build()
    );

    // Anti-DDoS Protection Settings
    private final Setting<Boolean> enableRateLimit = sgGeneral.add(new BoolSetting.Builder()
        .name("enable-rate-limit")
        .description("Enable rate limiting for API calls to prevent abuse.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> apiCooldownSeconds = sgGeneral.add(new IntSetting.Builder()
        .name("api-cooldown-seconds")
        .description("Minimum seconds between API calls.")
        .defaultValue(2)
        .min(1)
        .max(30)
        .sliderRange(1, 10)
        .build()
    );

    private final Setting<Boolean> enableCache = sgGeneral.add(new BoolSetting.Builder()
        .name("enable-cache")
        .description("Enable caching of API responses to reduce requests.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> cacheSize = sgGeneral.add(new IntSetting.Builder()
        .name("cache-size")
        .description("Maximum number of insults to cache.")
        .defaultValue(20)
        .min(5)
        .max(100)
        .sliderRange(10, 50)
        .build()
    );

    private final Setting<Integer> cacheExpireMinutes = sgGeneral.add(new IntSetting.Builder()
        .name("cache-expire-minutes")
        .description("Minutes before cached insults expire.")
        .defaultValue(10)
        .min(1)
        .max(60)
        .sliderRange(5, 30)
        .build()
    );

    private final Setting<Boolean> enableAsyncApi = sgGeneral.add(new BoolSetting.Builder()
        .name("enable-async-api")
        .description("Use asynchronous API calls to prevent game freezing.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> disableApiOnError = sgGeneral.add(new BoolSetting.Builder()
        .name("disable-api-on-error")
        .description("Automatically disable API calls after multiple failures.")
        .defaultValue(true)
        .build()
    );

    private final Set<String> defaultInsults = new HashSet<>(Arrays.asList(
        "idiot", "stupid", "dumb", "moron", "fool", "idiot", "retard", "loser",
        "noob", "newb", "suck", "garbage", "trash", "pathetic", "useless"
    ));

    private final Pattern insultPattern;

    // Anti-DDoS Protection Infrastructure
    private Instant lastApiCall = Instant.MIN;
    private final Map<String, CachedInsult> insultCache = new ConcurrentHashMap<>();
    private final Queue<String> cacheOrder = new LinkedList<>();
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private final AtomicBoolean apiDisabled = new AtomicBoolean(false);
    private final Queue<CompletableFuture<String>> pendingRequests = new LinkedList<>();
    private static final int MAX_CONSECUTIVE_FAILURES = 5;
    private static final int MAX_PENDING_REQUESTS = 10;

    // Cache entry with expiration
    private static class CachedInsult {
        final String insult;
        final Instant expiresAt;

        CachedInsult(String insult, Instant expiresAt) {
            this.insult = insult;
            this.expiresAt = expiresAt;
        }

        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    public Insulit() {
        super(AddonTemplate.CATEGORY, "insulit", "Filters insults from chat to protect from toxicity.");

        StringBuilder regex = new StringBuilder();
        for (String insult : defaultInsults) {
            if (regex.length() > 0) regex.append("|");
            regex.append("\\b").append(Pattern.quote(insult)).append("\\b");
        }
        insultPattern = Pattern.compile(regex.toString(), Pattern.CASE_INSENSITIVE);

        // Schedule cache cleanup every 5 minutes
        executorService.scheduleAtFixedRate(() -> cleanupExpiredCache(), 5, 5, TimeUnit.MINUTES);
    }

    @Override
    public void onDeactivate() {
        // Clean up resources when module is disabled
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        // Clear cache and reset state
        insultCache.clear();
        cacheOrder.clear();
        pendingRequests.clear();
        consecutiveFailures.set(0);
        apiDisabled.set(false);
        lastApiCall = Instant.MIN;
    }

    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent event) {
        if (!filterIncoming.get()) return;

        String message = event.getMessage().getString();
        String filtered = filterInsults(message);
        
        if (!message.equals(filtered)) {
            event.setMessage(Text.literal(filtered));
        }
    }

    @EventHandler
    private void onSendMessage(SendMessageEvent event) {
        if (!filterOutgoing.get()) return;

        String message = event.message;
        String filtered = filterInsults(message);
        
        if (!message.equals(filtered)) {
            event.message = filtered;
        }
    }

    private String filterInsults(String input) {
        String result = input;

        // Filter default insults
        result = insultPattern.matcher(result).replaceAll(match -> {
            if (replaceWithApiInsult.get()) {
                return fetchInsultFromApiWithProtection();
            } else {
                String insult = match.group();
                return replaceWithStars.get() ? createStars(insult.length()) : "";
            }
        });

        // Filter custom insults if enabled
        if (customWords.get()) {
            for (String insult : customInsults.get()) {
                Pattern customPattern = Pattern.compile("\\b" + Pattern.quote(insult.toLowerCase()) + "\\b", Pattern.CASE_INSENSITIVE);
                result = customPattern.matcher(result).replaceAll(match -> {
                    if (replaceWithApiInsult.get()) {
                        return fetchInsultFromApiWithProtection();
                    } else {
                        String matched = match.group();
                        return replaceWithStars.get() ? createStars(matched.length()) : "";
                    }
                });
            }
        }

        return result;
    }

    private String createStars(int length) {
        return "*".repeat(length);
    }

    private void cleanupExpiredCache() {
        insultCache.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().isExpired();
            if (expired) {
                cacheOrder.remove(entry.getKey());
            }
            return expired;
        });
    }

    private String getCachedInsult() {
        if (!enableCache.get()) return null;
        
        cleanupExpiredCache();
        
        if (!insultCache.isEmpty()) {
            // Get a random cached insult
            List<String> keys = new ArrayList<>(insultCache.keySet());
            String randomKey = keys.get(ThreadLocalRandom.current().nextInt(keys.size()));
            CachedInsult cached = insultCache.get(randomKey);
            if (cached != null && !cached.isExpired()) {
                return cached.insult;
            }
        }
        return null;
    }

    private void cacheInsult(String insult) {
        if (!enableCache.get() || insult == null || insult.trim().isEmpty()) return;
        
        String key = "insult_" + System.currentTimeMillis() + "_" + ThreadLocalRandom.current().nextInt(1000);
        Instant expiresAt = Instant.now().plus(cacheExpireMinutes.get(), ChronoUnit.MINUTES);
        
        // Remove oldest if cache is full
        if (insultCache.size() >= cacheSize.get()) {
            String oldestKey = cacheOrder.poll();
            if (oldestKey != null) {
                insultCache.remove(oldestKey);
            }
        }
        
        insultCache.put(key, new CachedInsult(insult, expiresAt));
        cacheOrder.add(key);
    }

    private boolean canMakeApiCall() {
        if (apiDisabled.get()) return false;
        
        if (enableRateLimit.get()) {
            Instant now = Instant.now();
            long secondsSinceLastCall = ChronoUnit.SECONDS.between(lastApiCall, now);
            if (secondsSinceLastCall < apiCooldownSeconds.get()) {
                return false;
            }
        }
        
        if (pendingRequests.size() >= MAX_PENDING_REQUESTS) {
            return false;
        }
        
        return true;
    }

    private String fetchInsultFromApiWithProtection() {
        // Check cache first
        String cached = getCachedInsult();
        if (cached != null) {
            return cached;
        }
        
        // Check if we can make an API call
        if (!canMakeApiCall()) {
            return getFallbackInsult();
        }
        
        // Update last call time
        lastApiCall = Instant.now();
        
        if (enableAsyncApi.get()) {
            return fetchInsultAsync();
        } else {
            return fetchInsultSync();
        }
    }

    private String fetchInsultAsync() {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                return fetchInsultFromApi();
            } catch (Exception e) {
                handleApiFailure(e);
                return getFallbackInsult();
            }
        }, executorService);
        
        pendingRequests.add(future);
        
        try {
            // Wait up to 3 seconds for async result
            String result = future.get(3, TimeUnit.SECONDS);
            if (result != null && !result.trim().isEmpty()) {
                cacheInsult(result);
                consecutiveFailures.set(0);
                return result;
            }
        } catch (Exception e) {
            handleApiFailure(e);
        } finally {
            pendingRequests.remove(future);
        }
        
        return getFallbackInsult();
    }

    private String fetchInsultSync() {
        try {
            String result = fetchInsultFromApi();
            if (result != null && !result.trim().isEmpty()) {
                cacheInsult(result);
                consecutiveFailures.set(0);
                return result;
            }
        } catch (Exception e) {
            handleApiFailure(e);
        }
        return getFallbackInsult();
    }

    private void handleApiFailure(Exception e) {
        int failures = consecutiveFailures.incrementAndGet();
        warning("API call failed (" + failures + "/" + MAX_CONSECUTIVE_FAILURES + "): " + e.getMessage());
        
        if (failures >= MAX_CONSECUTIVE_FAILURES && disableApiOnError.get()) {
            apiDisabled.set(true);
            error("API disabled after " + MAX_CONSECUTIVE_FAILURES + " consecutive failures. Restart module to re-enable.");
        }
    }

    private String getFallbackInsult() {
        String[] fallbacks = {
            "You are a smelly pirate!",
            "Your mother was a hamster!",
            "Your father smelt of elderberries!",
            "I fart in your general direction!",
            "You silly English kniggit!"
        };
        return fallbacks[ThreadLocalRandom.current().nextInt(fallbacks.length)];
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
