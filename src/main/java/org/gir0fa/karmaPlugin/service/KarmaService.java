package org.gir0fa.karmaPlugin.service;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.gir0fa.karmaPlugin.model.Alignment;
import org.gir0fa.karmaPlugin.storage.KarmaStorage;
import org.gir0fa.karmaPlugin.util.ConfigKeys;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class KarmaService {

    public interface KarmaChangeListener {
        void onKarmaChange(UUID playerId, int oldValue, int newValue);
        void onAlignmentChange(UUID playerId, Alignment oldAlignment, Alignment newAlignment);
    }

    private final Plugin plugin;
    private final KarmaStorage storage;
    private FileConfiguration config;

    private final Set<EntityType> evilMobs = EnumSet.noneOf(EntityType.class);
    private final List<KarmaChangeListener> listeners = new CopyOnWriteArrayList<>();

    private int maxAbsKarma;
    private int goodThreshold;
    private int evilThreshold;

    private int valueKillPlayer;
    private int valueKillVillager;
    private int valueKillEvilMob;

    public KarmaService(Plugin plugin, KarmaStorage storage, FileConfiguration config) {
        this.plugin = plugin;
        this.storage = storage;
        this.config = config;
        loadConfig();
    }

    public void reloadConfig(FileConfiguration newConfig) {
        this.config = newConfig;
        loadConfig();
    }

    private void loadConfig() {
        this.maxAbsKarma = config.getInt(ConfigKeys.VALUES_MAX_ABS_KARMA, 9999);
        this.goodThreshold = config.getInt(ConfigKeys.THRESHOLDS_GOOD, 50);
        this.evilThreshold = config.getInt(ConfigKeys.THRESHOLDS_EVIL, -50);

        this.valueKillPlayer = config.getInt(ConfigKeys.VALUES_KILL_PLAYER, -20);
        this.valueKillVillager = config.getInt(ConfigKeys.VALUES_KILL_VILLAGER, -15);
        this.valueKillEvilMob = config.getInt(ConfigKeys.VALUES_KILL_EVIL_MOB, 10);

        evilMobs.clear();
        for (String mob : config.getStringList(ConfigKeys.EVIL_MOBS)) {
            try {
                evilMobs.add(EntityType.valueOf(mob.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Invalid EntityType in evil_mobs: " + mob);
            }
        }
    }

    public void registerListener(KarmaChangeListener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(KarmaChangeListener listener) { listeners.remove(listener); }

    public int getKarma(UUID playerId) {
        return storage.getKarma(playerId);
    }

    public Alignment getAlignment(UUID playerId) {
        int karma = getKarma(playerId);
        if (karma >= goodThreshold) return Alignment.GOOD;
        if (karma <= evilThreshold) return Alignment.EVIL;
        return Alignment.NEUTRAL;
    }

    public void setKarma(UUID playerId, int value) {
        int clamped = Math.max(-maxAbsKarma, Math.min(maxAbsKarma, value));
        int old = storage.getKarma(playerId);
        if (old == clamped) return;
        Alignment oldAlignment = alignmentForValue(old);
        Alignment newAlignment = alignmentForValue(clamped);
        storage.setKarma(playerId, clamped);
        if (!Objects.equals(oldAlignment, newAlignment)) {
            for (KarmaChangeListener l : listeners) {
                try { l.onAlignmentChange(playerId, oldAlignment, newAlignment); } catch (Throwable t) { t.printStackTrace(); }
            }
        }
        for (KarmaChangeListener l : listeners) {
            try { l.onKarmaChange(playerId, old, clamped); } catch (Throwable t) { t.printStackTrace(); }
        }
    }

    public void addKarma(UUID playerId, int delta) {
        setKarma(playerId, getKarma(playerId) + delta);
    }

    private Alignment alignmentForValue(int value) {
        if (value >= goodThreshold) return Alignment.GOOD;
        if (value <= evilThreshold) return Alignment.EVIL;
        return Alignment.NEUTRAL;
    }

    public boolean isEvilMob(EntityType entityType) {
        return evilMobs.contains(entityType);
    }

    public int getValueKillPlayer() { return valueKillPlayer; }
    public int getValueKillVillager() { return valueKillVillager; }
    public int getValueKillEvilMob() { return valueKillEvilMob; }

    public int getGoodThreshold() { return goodThreshold; }
    public int getEvilThreshold() { return evilThreshold; }

    public int getMaxAbsKarma() { return maxAbsKarma; }

    public void saveKarmaData() { storage.saveSync(); }
}


