package com.kaboomfilter;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Updater {
    private static final String CURRENT_VERSION = "1.0.0"; // Update this with the current version
    private static final String GITHUB_REPO = "GreatestDisplayName/meteor-addon"; // Replace with actual GitHub repo: owner/repo
    private static final HttpClient client = HttpClient.newHttpClient();

    public static boolean checkForUpdate() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/repos/" + GITHUB_REPO + "/releases/latest"))
                .header("Accept", "application/vnd.github.v3+json")
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String json = response.body();
                // Extract tag_name from JSON
                Pattern pattern = Pattern.compile("\"tag_name\":\"([^\"]+)\"");
                Matcher matcher = pattern.matcher(json);
                if (matcher.find()) {
                    String latestVersion = matcher.group(1).replace("v", ""); // Remove 'v' prefix if present
                    return isNewerVersion(latestVersion, CURRENT_VERSION);
                }
            }
        } catch (Exception e) {
            // Handle exceptions, e.g., network issues
            System.err.println("Failed to check for updates: " + e.getMessage());
        }
        return false;
    }

    private static boolean isNewerVersion(String latest, String current) {
        // Simple semantic version comparison (major.minor.patch)
        String[] latestParts = latest.split("\\.");
        String[] currentParts = current.split("\\.");

        int length = Math.max(latestParts.length, currentParts.length);
        for (int i = 0; i < length; i++) {
            int l = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;
            int c = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
            if (l > c) return true;
            if (l < c) return false;
        }
        return false;
    }

    public static void main(String[] args) {
        // Example usage
        if (checkForUpdate()) {
            System.out.println("A new version is available!");
        } else {
            System.out.println("You are up to date.");
        }
    }
}
