package com.example.addon.utils;

import meteordevelopment.meteorclient.systems.modules.Module;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class SlaveSystem {
    public static String directMessageCommand;
    public static String senderPrefix;
    public static String senderSuffix;
    public static int commandDelay;
    public static int randomLength;

    public static List<String> slaves = new ArrayList<>();
    public static HashMap<String, Object> activeSlavesDict = new HashMap<>();
    public static HashMap<String, Object> finishedSlavesDict = new HashMap<>();
    public static Object tableController;

    public static void setupSlaveSystem(Module module, int delay, String cmd, String prefix, String suffix, int random) {
        commandDelay = delay;
        directMessageCommand = cmd;
        senderPrefix = prefix;
        senderSuffix = suffix;
        randomLength = random;
    }

    public static boolean allSlavesFinished() {
        return true; // Stub
    }

    public static void queueDM(String s1, String s2) {
        // Stub
    }

    public static boolean isSlave() {
        return false; // Stub
    }

    public static void queueMasterDM(String s) {
        // Stub
    }

    public static void startAllSlaves() {
        // Stub
    }

    public static void setAllSlavesUnfinished() {
        // Stub
    }

    public static void sendToAllSlaves(String s) {
        // Stub
    }
}
