package com.gir0fa.karmaPlugin.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import com.gir0fa.karmaPlugin.model.Alignment;
import com.gir0fa.karmaPlugin.service.KarmaService;
import com.gir0fa.karmaPlugin.util.ConfigKeys;

public class ChatListener implements Listener {

    private final Plugin plugin;
    private final KarmaService karmaService;

    public ChatListener(Plugin plugin, KarmaService karmaService) {
        this.plugin = plugin;
        this.karmaService = karmaService;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!plugin.getConfig().getBoolean(ConfigKeys.DISPLAY_CHAT_ENABLED, false)) return;
        Player player = event.getPlayer();
        Alignment alignment = karmaService.getAlignment(player.getUniqueId());
        String prefixPath = switch (alignment) {
            case GOOD -> ConfigKeys.DISPLAY_NAME_TAG_GOOD_PREFIX;
            case EVIL -> ConfigKeys.DISPLAY_NAME_TAG_EVIL_PREFIX;
            default -> ConfigKeys.DISPLAY_NAME_TAG_NEUTRAL_PREFIX;
        };
        String prefix = ConfigKeys.colorize(plugin.getConfig().getString(prefixPath, ""));
        // Keep vanilla placeholders so other plugins can still intercept/format around "%1$s: %2$s"
        event.setFormat(prefix + ChatColor.RESET + "%1$s: %2$s");
    }
}


