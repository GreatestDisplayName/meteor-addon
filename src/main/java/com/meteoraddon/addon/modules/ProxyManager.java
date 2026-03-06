package com.meteoraddon.addon.modules;

import com.meteoraddon.addon.AddonTemplate;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.text.Text;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import java.util.List;

public class ProxyManager extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<String>> proxyList = sgGeneral.add(new StringListSetting.Builder()
        .name("proxy-list")
        .description("List of proxies in IP:PORT format.")
        .defaultValue(java.util.Arrays.asList("127.0.0.1:8080"))
        .build()
    );

    private final Setting<Integer> selectedIndex = sgGeneral.add(new IntSetting.Builder()
        .name("selected-index")
        .description("Index of selected proxy.")
        .defaultValue(0)
        .min(0)
        .build()
    );

    private final Setting<Boolean> useTor = sgGeneral.add(new BoolSetting.Builder()
        .name("use-tor")
        .description("Use Tor as proxy.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> enableApiProxy = sgGeneral.add(new BoolSetting.Builder()
        .name("enable-api-proxy")
        .description("Enable proxy for addon API calls.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> rotateOnActivate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate-on-activate")
        .description("Rotate to next proxy on activate.")
        .defaultValue(false)
        .build()
    );

    public ProxyManager() {
        super(AddonTemplate.CATEGORY, "proxy-manager", "Manages proxies for the addon.");
    }

    @Override
    public void onActivate() {
        if (rotateOnActivate.get()) {
            rotateProxy();
        }
        setCurrentProxy();
        testProxy();
        toggle(); // Disable after setup
    }

    private void rotateProxy() {
        List<String> list = proxyList.get();
        if (!list.isEmpty()) {
            int newIndex = (selectedIndex.get() + 1) % list.size();
            selectedIndex.set(newIndex);
        }
    }

    private void setCurrentProxy() {
        if (!enableApiProxy.get()) {
            AddonTemplate.currentProxy = null;
            ChatUtils.sendMsg(Text.literal("API proxy disabled."));
            return;
        }
        if (useTor.get()) {
            AddonTemplate.currentProxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 9050));
            ChatUtils.sendMsg(Text.literal("Using Tor proxy for API calls."));
        } else {
            List<String> list = proxyList.get();
            int index = selectedIndex.get();
            if (index >= 0 && index < list.size()) {
                String proxyStr = list.get(index);
                String[] parts = proxyStr.split(":");
                if (parts.length == 2) {
                    try {
                        String host = parts[0];
                        int port = Integer.parseInt(parts[1]);
                        AddonTemplate.currentProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
                        ChatUtils.sendMsg(Text.literal("Using proxy: " + proxyStr + " for API calls."));
                    } catch (NumberFormatException e) {
                        ChatUtils.sendMsg(Text.literal("Invalid proxy format: " + proxyStr));
                        AddonTemplate.currentProxy = null;
                    }
                } else {
                    ChatUtils.sendMsg(Text.literal("Invalid proxy format: " + proxyStr));
                    AddonTemplate.currentProxy = null;
                }
            } else {
                ChatUtils.sendMsg(Text.literal("No valid proxy selected."));
                AddonTemplate.currentProxy = null;
            }
        }
    }

    private void testProxy() {
        try {
            URL url = new URI("https://httpbin.org/ip").toURL();
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection(AddonTemplate.currentProxy);
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                ChatUtils.sendMsg(Text.literal("Proxy test successful."));
            } else {
                ChatUtils.sendMsg(Text.literal("Proxy test failed with code: " + responseCode));
            }
            connection.disconnect();
        } catch (IOException | URISyntaxException e) {
            ChatUtils.sendMsg(Text.literal("Proxy test failed: " + e.getMessage()));
        }
    }
}
