package com.gir0fa.karmaPlugin.storage;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class YamlKarmaStorage implements KarmaStorage {

    private final JavaPlugin plugin;
    private final File karmaFile;
    private FileConfiguration karmaConfig;

    private final AtomicBoolean saveQueued = new AtomicBoolean(false);

    public YamlKarmaStorage(JavaPlugin plugin) {
        this.plugin = plugin;
        this.karmaFile = new File(plugin.getDataFolder(), "karma.yml");
        if (!karmaFile.exists()) {
            try {
                karmaFile.getParentFile().mkdirs();
                karmaFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create karma.yml: " + e.getMessage());
            }
        }
        this.karmaConfig = YamlConfiguration.loadConfiguration(karmaFile);
    }

    @Override
    public int getKarma(UUID playerId) {
        return karmaConfig.getInt(playerId.toString(), 0);
    }

    @Override
    public void setKarma(UUID playerId, int karma) {
        karmaConfig.set(playerId.toString(), karma);
        saveAsync();
    }

    @Override
    public void saveAsync() {
        if (saveQueued.getAndSet(true)) {
            return; // already queued
        }
        // Debounce by 40 ticks (~2s)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            saveSync();
            saveQueued.set(false);
        }, 40L);
    }

    @Override
    public void saveSync() {
        try {
            karmaConfig.save(karmaFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save karma.yml: " + e.getMessage());
        }
    }
}


