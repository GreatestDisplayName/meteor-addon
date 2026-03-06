package com.meteoraddon.addon.modules;

import com.meteoraddon.addon.AddonTemplate;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;

import net.minecraft.text.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

public class ProxyScraper extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> sourceUrl = sgGeneral.add(new StringSetting.Builder()
        .name("source-url")
        .description("URL to scrape proxies from.")
        .defaultValue("https://api.proxyscrape.com/v2/?request=getproxies&protocol=http&timeout=5000&country=all&ssl=all&anonymity=all")
        .build()
    );

    public ProxyScraper() {
        super(AddonTemplate.CATEGORY, "proxy-scraper", "Scrapes proxies from online sources.");
    }

    @Override
    public void onActivate() {
        String proxies = fetchProxies();
        if (proxies != null && !proxies.isEmpty()) {
            ChatUtils.sendMsg(Text.literal("Fetched proxies: " + proxies.replace("\n", ", ")));
        } else {
            ChatUtils.sendMsg(Text.literal("Failed to fetch proxies."));
        }
        toggle(); // Disable after fetch
    }

    private String fetchProxies() {
        try {
            URL url = new URI(sourceUrl.get()).toURL();
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000); // 10 seconds
            connection.setReadTimeout(10000); // 10 seconds

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine).append("\n");
                }
                in.close();
                connection.disconnect();
                return content.toString();
            } else {
                error("Failed to fetch proxies. Response code: " + responseCode);
            }
        } catch (IOException | URISyntaxException e) {
            error("Error fetching proxies: " + e.getMessage());
        }
        return null;
    }
}
