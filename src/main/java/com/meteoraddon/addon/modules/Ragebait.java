package com.meteoraddon.addon.modules;

import com.meteoraddon.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
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

    public Ragebait() {
        super(AddonTemplate.CATEGORY, "ragebait", "Generates toxic messages using API insults.");
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
        String insult = fetchInsultFromApi();
        return insult;
    }

    private String fetchInsultFromApi() {
        try {
            URL url = new URI("https://evilinsult.com/generate_insult.php?lang=en&type=plain%20text").toURL();
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection(AddonTemplate.currentProxy);
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
