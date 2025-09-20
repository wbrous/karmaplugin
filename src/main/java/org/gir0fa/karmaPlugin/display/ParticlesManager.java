package org.gir0fa.karmaPlugin.display;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.gir0fa.karmaPlugin.model.Alignment;
import org.gir0fa.karmaPlugin.service.KarmaService;
import org.gir0fa.karmaPlugin.util.ConfigKeys;

public class ParticlesManager {

    private final Plugin plugin;
    private final KarmaService karmaService;
    private boolean enabled;

    public ParticlesManager(Plugin plugin, KarmaService karmaService) {
        this.plugin = plugin;
        this.karmaService = karmaService;
        reload();
    }

    public void reload() {
        this.enabled = plugin.getConfig().getBoolean(ConfigKeys.DISPLAY_PARTICLES_ENABLED, true);
    }

    public void emitFor(Player player) {
        if (!enabled) return;
        Alignment alignment = karmaService.getAlignment(player.getUniqueId());
        Particle.DustOptions dust;
        switch (alignment) {
            case GOOD -> dust = new Particle.DustOptions(Color.fromRGB(85, 255, 85), 1.0f);
            case EVIL -> dust = new Particle.DustOptions(Color.fromRGB(255, 85, 85), 1.0f);
            default -> dust = new Particle.DustOptions(Color.fromRGB(200, 200, 200), 1.0f);
        }
        player.getWorld().spawnParticle(Particle.DUST, player.getLocation().add(0, 1.0, 0), 6, 0.3, 0.3, 0.3, 0.01, dust);
    }

    public void cleanup() {
        // No persistent state
    }
}


