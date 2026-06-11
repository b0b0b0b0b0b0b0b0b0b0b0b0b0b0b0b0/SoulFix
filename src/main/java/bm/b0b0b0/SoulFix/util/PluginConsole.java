package bm.b0b0b0.SoulFix.util;

import org.bukkit.Bukkit;

public final class PluginConsole {

    public static final String PREFIX = "\u001B[37m[\u001B[90mSoulFix\u001B[37m]\u001B[0m ";

    private static final String GRAY = "\u001B[90m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RESET = "\u001B[0m";

    private PluginConsole() {
    }

    public static void blankLine() {
        Bukkit.getConsoleSender().sendMessage(" ");
    }

    public static void separator() {
        Bukkit.getConsoleSender().sendMessage(PREFIX + "==============================");
    }

    public static void info(String message) {
        Bukkit.getConsoleSender().sendMessage(PREFIX + message);
    }

    public static void step(String message) {
        Bukkit.getConsoleSender().sendMessage(PREFIX + GRAY + message + RESET);
    }

    public static void success(String message) {
        Bukkit.getConsoleSender().sendMessage(PREFIX + GREEN + message + RESET);
    }

    public static void warn(String message) {
        Bukkit.getConsoleSender().sendMessage(PREFIX + YELLOW + message + RESET);
    }

    public static void error(String message) {
        Bukkit.getConsoleSender().sendMessage(PREFIX + RED + message + RESET);
    }

    public static void errorBlock(String message) {
        Bukkit.getConsoleSender().sendMessage(PREFIX + RED + "========================================================" + RESET);
        Bukkit.getConsoleSender().sendMessage(PREFIX + RED + message + RESET);
        Bukkit.getConsoleSender().sendMessage(PREFIX + RED + "========================================================" + RESET);
    }

    public static void startupHeader(String version) {
        blankLine();
        separator();
        info("SoulFix is starting on your server");
        info("Version:" + GRAY + " " + version + " " + RESET + "| Author: " + GRAY + "b0b0b0" + RESET);
        blankLine();
        info("Initialization:");
    }

    public static void startupComplete() {
        success("SoulFix successfully loaded");
        separator();
        blankLine();
    }

    public static void shutdownHeader() {
        blankLine();
        separator();
        info("SoulFix is shutting down");
    }

    public static void shutdownComplete() {
        success("SoulFix successfully unloaded");
        separator();
        blankLine();
    }
}
