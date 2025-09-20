package org.gir0fa.karmaPlugin.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.Plugin;
import org.gir0fa.karmaPlugin.display.BossBarManager;
import org.gir0fa.karmaPlugin.display.NameTagManager;
import org.gir0fa.karmaPlugin.display.ParticlesManager;
import org.gir0fa.karmaPlugin.model.Alignment;
import org.gir0fa.karmaPlugin.service.KarmaService;

public class KarmaEffectsTask extends BukkitRunnable {

    private final Plugin plugin;
    private final KarmaService karmaService;
    private final BossBarManager bossBarManager;
    private final NameTagManager nameTagManager;
    private final ParticlesManager particlesManager;

    public KarmaEffectsTask(Plugin plugin, KarmaService karmaService, BossBarManager bossBarManager, NameTagManager nameTagManager, ParticlesManager particlesManager) {
        this.plugin = plugin;
        this.karmaService = karmaService;
        this.bossBarManager = bossBarManager;
        this.nameTagManager = nameTagManager;
        this.particlesManager = particlesManager;
    }

    @Override
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Alignment alignment = karmaService.getAlignment(p.getUniqueId());

            // Max health adjustment: EVIL -> 14.0 (7 hearts), others -> 20.0
            double targetMax = (alignment == Alignment.EVIL) ? karmaService.getEvilMaxHealth() : 20.0;
            AttributeInstance maxHealth = p.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (maxHealth != null && maxHealth.getBaseValue() != targetMax) {
                maxHealth.setBaseValue(targetMax);
                if (p.getHealth() > targetMax) {
                    p.setHealth(targetMax);
                }
            }

            // Effects ~6 seconds so they bridge a 5 second interval
            if (alignment == Alignment.GOOD) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 6, 0, true, false, true));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 6, 0, true, false, true));
            } else if (alignment == Alignment.EVIL) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20 * 6, 0, true, false, true));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 6, 0, true, false, true));
            }

            bossBarManager.updatePlayer(p.getUniqueId());
            nameTagManager.assignPlayerToAlignmentTeam(p.getUniqueId());
            particlesManager.emitFor(p);
        }
    }
}


