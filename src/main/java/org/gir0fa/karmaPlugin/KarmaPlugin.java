package org.gir0fa.karmaPlugin;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.gir0fa.karmaPlugin.commands.KarmaCommand;
import org.gir0fa.karmaPlugin.display.BossBarManager;
import org.gir0fa.karmaPlugin.display.NameTagManager;
import org.gir0fa.karmaPlugin.display.ParticlesManager;
import org.gir0fa.karmaPlugin.listeners.KarmaListener;
import org.gir0fa.karmaPlugin.listeners.ChatListener;
import org.gir0fa.karmaPlugin.model.Alignment;
import org.gir0fa.karmaPlugin.scheduler.KarmaEffectsTask;
import org.gir0fa.karmaPlugin.service.KarmaService;
import org.gir0fa.karmaPlugin.storage.YamlKarmaStorage;
import org.gir0fa.karmaPlugin.util.ConfigKeys;

import java.util.UUID;

public class KarmaPlugin extends JavaPlugin {

    private KarmaService karmaService;
    private BossBarManager bossBarManager;
    private NameTagManager nameTagManager;
    private ParticlesManager particlesManager;
    private KarmaEffectsTask effectsTask;

    @Override
    public void onEnable() {
        getLogger().info("Enabling KarmaSMP...");
        saveDefaultConfig();

        // Storage and service
        YamlKarmaStorage storage = new YamlKarmaStorage(this);
        karmaService = new KarmaService(this, storage, getConfig());

        // Managers
        bossBarManager = new BossBarManager(this, karmaService);
        nameTagManager = new NameTagManager(this, karmaService);
        particlesManager = new ParticlesManager(this, karmaService);

        // Subscribe to karma change notifications for live UI updates
        karmaService.registerListener(new KarmaService.KarmaChangeListener() {
            @Override
            public void onKarmaChange(UUID playerId, int oldValue, int newValue) {
                bossBarManager.updatePlayer(playerId);
            }

            @Override
            public void onAlignmentChange(UUID playerId, Alignment oldAlignment, Alignment newAlignment) {
                bossBarManager.updatePlayer(playerId);
                nameTagManager.assignPlayerToAlignmentTeam(playerId);
                // Apply health cap immediately on alignment change
                var player = getServer().getPlayer(playerId);
                if (player != null) {
                    getServer().getScheduler().runTask(KarmaPlugin.this, () -> {
                        try {
                            double targetMax = (newAlignment == Alignment.EVIL) ? karmaService.getEvilMaxHealth() : 20.0;
                            var inst = player.getAttribute(Attribute.MAX_HEALTH);
                            if (inst != null && inst.getBaseValue() != targetMax) {
                                inst.setBaseValue(targetMax);
                                if (player.getHealth() > targetMax) player.setHealth(targetMax);
                            }
                        } catch (Throwable ignored) {}
                    });
                }
            }
        });

        // Listeners
        getServer().getPluginManager().registerEvents(new KarmaListener(this, karmaService, bossBarManager, nameTagManager), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this, karmaService), this);

        // Command
        PluginCommand cmd = getCommand("karma");
        if (cmd != null) {
            KarmaCommand executor = new KarmaCommand(this, karmaService);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        } else {
            getLogger().severe("Command 'karma' missing from plugin.yml");
        }

        // Initialize displays for currently online players (e.g., during /reload)
        bossBarManager.initializeForOnlinePlayers();
        nameTagManager.refreshAllOnline();

        // Schedule periodic effects and display updates
        scheduleEffectsTask();

        getLogger().info("KarmaSMP enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling KarmaSMP...");
        if (effectsTask != null) {
            effectsTask.cancel();
            effectsTask = null;
        }
        if (bossBarManager != null) bossBarManager.cleanup();
        if (nameTagManager != null) nameTagManager.cleanup();
        if (particlesManager != null) particlesManager.cleanup();
        if (karmaService != null) karmaService.saveKarmaData();
        getLogger().info("KarmaSMP disabled.");
    }

    public void reloadPlugin() {
        getLogger().info("Reloading KarmaSMP config...");
        reloadConfig();
        FileConfiguration cfg = getConfig();
        karmaService.reloadConfig(cfg);
        bossBarManager.reload();
        nameTagManager.reload();
        particlesManager.reload();
        // Reschedule effects
        if (effectsTask != null) {
            effectsTask.cancel();
            effectsTask = null;
        }
        scheduleEffectsTask();
        // Update all online players immediately after reload
        bossBarManager.updateAll();
        nameTagManager.refreshAllOnline();
        getLogger().info("KarmaSMP reload complete.");
    }

    private void scheduleEffectsTask() {
        int intervalSeconds = getConfig().getInt(ConfigKeys.INTERVAL_SECONDS, 5);
        long periodTicks = Math.max(1, intervalSeconds) * 20L;
        effectsTask = new KarmaEffectsTask(this, karmaService, bossBarManager, nameTagManager, particlesManager);
        effectsTask.runTaskTimer(this, periodTicks, periodTicks);
    }
}
