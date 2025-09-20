package org.gir0fa.karmaPlugin.display;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Team;
import org.gir0fa.karmaPlugin.model.Alignment;
import org.gir0fa.karmaPlugin.service.KarmaService;
import org.gir0fa.karmaPlugin.util.ConfigKeys;

import java.util.UUID;

public class NameTagManager {

    public static final String TEAM_GOOD = "karma_good";
    public static final String TEAM_NEUTRAL = "karma_neutral";
    public static final String TEAM_EVIL = "karma_evil";

    private final Plugin plugin;
    private final KarmaService karmaService;
    private boolean nameTagEnabled;
    private boolean nameColorEnabled;

    private String prefixGood;
    private String prefixNeutral;
    private String prefixEvil;

    public NameTagManager(Plugin plugin, KarmaService karmaService) {
        this.plugin = plugin;
        this.karmaService = karmaService;
        reload();
        ensureTeams();
    }

    public void reload() {
        this.nameTagEnabled = plugin.getConfig().getBoolean(ConfigKeys.DISPLAY_NAME_TAG_ENABLED, true);
        this.nameColorEnabled = plugin.getConfig().getBoolean(ConfigKeys.DISPLAY_NAME_COLOR_ENABLED, true);
        this.prefixGood = ConfigKeys.colorize(plugin.getConfig().getString(ConfigKeys.DISPLAY_NAME_TAG_GOOD_PREFIX, "&a[Good] "));
        this.prefixNeutral = ConfigKeys.colorize(plugin.getConfig().getString(ConfigKeys.DISPLAY_NAME_TAG_NEUTRAL_PREFIX, "&7[Neutral] "));
        this.prefixEvil = ConfigKeys.colorize(plugin.getConfig().getString(ConfigKeys.DISPLAY_NAME_TAG_EVIL_PREFIX, "&c[Evil] "));
        ensureTeams();
    }

    private void ensureTeams() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;
        Scoreboard main = manager.getMainScoreboard();
        configureTeam(main, TEAM_GOOD, prefixGood, ChatColor.GREEN);
        configureTeam(main, TEAM_NEUTRAL, prefixNeutral, ChatColor.GRAY);
        configureTeam(main, TEAM_EVIL, prefixEvil, ChatColor.RED);
    }

    private void configureTeam(Scoreboard board, String teamName, String prefix, ChatColor color) {
        Team team = board.getTeam(teamName);
        if (team == null) team = board.registerNewTeam(teamName);
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        team.setPrefix(nameTagEnabled ? prefix : "");
        if (nameColorEnabled) {
            team.setColor(color);
        } else {
            // Reset to white if color disabled
            team.setColor(ChatColor.WHITE);
        }
    }

    public void assignPlayerToAlignmentTeam(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) return;
        Scoreboard board = Bukkit.getScoreboardManager() != null ? Bukkit.getScoreboardManager().getMainScoreboard() : null;
        if (board == null) return;

        Team good = board.getTeam(TEAM_GOOD);
        Team neutral = board.getTeam(TEAM_NEUTRAL);
        Team evil = board.getTeam(TEAM_EVIL);
        if (good == null || neutral == null || evil == null) return;

        // Remove from any
        good.removeEntry(player.getName());
        neutral.removeEntry(player.getName());
        evil.removeEntry(player.getName());

        Alignment alignment = karmaService.getAlignment(playerId);
        switch (alignment) {
            case GOOD -> good.addEntry(player.getName());
            case EVIL -> evil.addEntry(player.getName());
            default -> neutral.addEntry(player.getName());
        }
    }

    public void refreshAllOnline() {
        ensureTeams();
        for (Player p : Bukkit.getOnlinePlayers()) {
            assignPlayerToAlignmentTeam(p.getUniqueId());
        }
    }

    public void handleJoin(Player player) {
        assignPlayerToAlignmentTeam(player.getUniqueId());
    }

    public void handleQuit(Player player) {
        // Nothing special; leaving player entries in scoreboard is fine as SB persists globally
    }

    public void cleanup() {
        // Do not unregister teams (they are global). Just clear prefixes if feature disabled.
        ensureTeams();
    }
}


