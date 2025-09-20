package org.gir0fa.karmaPlugin.util;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public final class ConfigKeys {
    private ConfigKeys() {}

    // Thresholds
    public static final String THRESHOLDS_GOOD = "thresholds.good";
    public static final String THRESHOLDS_EVIL = "thresholds.evil";

    // Values
    public static final String VALUES_KILL_PLAYER = "values.kill_player";
    public static final String VALUES_KILL_VILLAGER = "values.kill_villager";
    public static final String VALUES_KILL_EVIL_MOB = "values.kill_evil_mob";
    public static final String VALUES_MAX_ABS_KARMA = "values.max_abs_karma";
    public static final String VALUES_EVIL_MAX_HEALTH = "values.evil_max_health";

    // Interval
    public static final String INTERVAL_SECONDS = "interval_seconds";

    // Display toggles and settings
    public static final String DISPLAY_BOSSBAR_ENABLED = "display.bossbar_enabled";
    public static final String DISPLAY_NAME_TAG_ENABLED = "display.name_tag_enabled";
    public static final String DISPLAY_NAME_COLOR_ENABLED = "display.name_color_enabled";
    public static final String DISPLAY_PARTICLES_ENABLED = "display.particles_enabled";
    public static final String DISPLAY_CHAT_ENABLED = "display.chat_enabled";
    public static final String DISPLAY_BOSSBAR_STYLE = "display.bossbar_style";

    public static final String DISPLAY_BOSSBAR_TITLES_GOOD = "display.bossbar_titles.good";
    public static final String DISPLAY_BOSSBAR_TITLES_NEUTRAL = "display.bossbar_titles.neutral";
    public static final String DISPLAY_BOSSBAR_TITLES_EVIL = "display.bossbar_titles.evil";

    public static final String DISPLAY_NAME_TAG_GOOD_PREFIX = "display.name_tag.good_prefix";
    public static final String DISPLAY_NAME_TAG_NEUTRAL_PREFIX = "display.name_tag.neutral_prefix";
    public static final String DISPLAY_NAME_TAG_EVIL_PREFIX = "display.name_tag.evil_prefix";

    // Colors
    public static final String COLORS_GOOD = "colors.good";
    public static final String COLORS_NEUTRAL = "colors.neutral";
    public static final String COLORS_EVIL = "colors.evil";

    // Evil mobs list
    public static final String EVIL_MOBS = "evil_mobs";

    public static String colorize(String input) {
        if (input == null) return "";
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    public static ChatColor parseColor(FileConfiguration config, String path, ChatColor fallback) {
        String text = config.getString(path);
        if (text == null || text.isEmpty()) return fallback;
        text = text.trim();
        // Accept '&a' style or names like 'GREEN'
        if (text.startsWith("&") && text.length() >= 2) {
            ChatColor byChar = ChatColor.getByChar(text.charAt(1));
            return byChar != null ? byChar : fallback;
        }
        try {
            return ChatColor.valueOf(text.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }
}


