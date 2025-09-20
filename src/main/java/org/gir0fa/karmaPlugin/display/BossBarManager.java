package org.gir0fa.karmaPlugin.display;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.gir0fa.karmaPlugin.model.Alignment;
import org.gir0fa.karmaPlugin.service.KarmaService;
import org.gir0fa.karmaPlugin.util.ConfigKeys;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BossBarManager {

    private final Plugin plugin;
    private final KarmaService karmaService;
    private final Map<UUID, BossBar> bars = new ConcurrentHashMap<>();

    private boolean enabled;
    private BarStyle barStyle;
    private final Map<Alignment, String> titles = new EnumMap<>(Alignment.class);

    public BossBarManager(Plugin plugin, KarmaService karmaService) {
        this.plugin = plugin;
        this.karmaService = karmaService;
        reload();
    }

    public void reload() {
        this.enabled = plugin.getConfig().getBoolean(ConfigKeys.DISPLAY_BOSSBAR_ENABLED, true);
        String styleName = plugin.getConfig().getString(ConfigKeys.DISPLAY_BOSSBAR_STYLE, "NOTCHED_12");
        try {
            this.barStyle = BarStyle.valueOf(styleName);
        } catch (IllegalArgumentException ex) {
            this.barStyle = BarStyle.SEGMENTED_12;
        }
        titles.put(Alignment.GOOD, plugin.getConfig().getString(ConfigKeys.DISPLAY_BOSSBAR_TITLES_GOOD, "Karma: %value% (Good)"));
        titles.put(Alignment.NEUTRAL, plugin.getConfig().getString(ConfigKeys.DISPLAY_BOSSBAR_TITLES_NEUTRAL, "Karma: %value% (Neutral)"));
        titles.put(Alignment.EVIL, plugin.getConfig().getString(ConfigKeys.DISPLAY_BOSSBAR_TITLES_EVIL, "Karma: %value% (Evil)"));

        // Update existing bars to new style and titles
        for (UUID uuid : bars.keySet()) {
            updatePlayer(uuid);
        }
    }

    public void initializeForOnlinePlayers() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            updatePlayer(p.getUniqueId());
        }
    }

    public void updateAll() {
        for (UUID uuid : bars.keySet()) {
            updatePlayer(uuid);
        }
    }

    public void updatePlayer(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) return;

        if (!enabled) {
            remove(playerId);
            return;
        }

        BossBar bar = bars.computeIfAbsent(playerId, id -> {
            BossBar created = Bukkit.createBossBar("Karma", BarColor.WHITE, barStyle);
            created.addPlayer(player);
            return created;
        });

        if (!bar.getPlayers().contains(player)) {
            bar.addPlayer(player);
        }

        int karma = karmaService.getKarma(playerId);
        Alignment alignment = karmaService.getAlignment(playerId);

        String titleTemplate = titles.getOrDefault(alignment, "Karma: %value%");
        String title = ChatColor.translateAlternateColorCodes('&', titleTemplate.replace("%value%", String.valueOf(karma)));
        bar.setTitle(title);

        switch (alignment) {
            case GOOD -> bar.setColor(BarColor.GREEN);
            case EVIL -> bar.setColor(BarColor.RED);
            default -> bar.setColor(BarColor.WHITE);
        }

        double progress = calculateProgress(karma);
        bar.setProgress(progress);
        bar.setStyle(barStyle);
        bar.setVisible(true);
    }

    private double calculateProgress(int karma) {
        int goodThreshold = karmaService.getGoodThreshold();
        int evilThreshold = karmaService.getEvilThreshold(); // negative

        // Center at 0.5
        if (karma == 0) return 0.5D;

        if (karma > 0) {
            double span = Math.max(1, goodThreshold);
            double ratio = Math.min(1.0, karma / span);
            return Math.max(0.0, Math.min(1.0, 0.5 + 0.5 * ratio));
        } else {
            double span = Math.max(1, Math.abs(evilThreshold));
            double ratio = Math.min(1.0, Math.abs(karma) / span);
            return Math.max(0.0, Math.min(1.0, 0.5 - 0.5 * ratio));
        }
    }

    public void remove(UUID playerId) {
        BossBar bar = bars.remove(playerId);
        if (bar != null) {
            for (Player p : bar.getPlayers()) {
                bar.removePlayer(p);
            }
            bar.setVisible(false);
        }
    }

    public void handleJoin(Player player) {
        updatePlayer(player.getUniqueId());
    }

    public void handleQuit(Player player) {
        remove(player.getUniqueId());
    }

    public void cleanup() {
        for (BossBar bar : bars.values()) {
            for (Player p : bar.getPlayers()) bar.removePlayer(p);
            bar.setVisible(false);
        }
        bars.clear();
    }
}


