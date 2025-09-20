package com.gir0fa.karmaPlugin.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.projectiles.ProjectileSource;
import com.gir0fa.karmaPlugin.display.BossBarManager;
import com.gir0fa.karmaPlugin.display.NameTagManager;
import com.gir0fa.karmaPlugin.model.Alignment;
import com.gir0fa.karmaPlugin.service.KarmaService;

public class KarmaListener implements Listener {

    private final org.bukkit.plugin.Plugin plugin;
    private final KarmaService karmaService;
    private final BossBarManager bossBarManager;
    private final NameTagManager nameTagManager;

    public KarmaListener(org.bukkit.plugin.Plugin plugin, KarmaService karmaService, BossBarManager bossBarManager, NameTagManager nameTagManager) {
        this.plugin = plugin;
        this.karmaService = karmaService;
        this.bossBarManager = bossBarManager;
        this.nameTagManager = nameTagManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        bossBarManager.handleJoin(player);
        nameTagManager.handleJoin(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        bossBarManager.handleQuit(player);
        nameTagManager.handleQuit(player);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer != null && killer != victim) {
            karmaService.addKarma(killer.getUniqueId(), karmaService.getValueKillPlayer());
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        // Villager killed by player
        if (entity instanceof Villager villager) {
            Player killer = villager.getKiller();
            if (killer == null && villager.getLastDamageCause() instanceof EntityDamageByEntityEvent edbe) {
                killer = resolveDamagerPlayer(edbe.getDamager());
            }
            if (killer != null) {
                karmaService.addKarma(killer.getUniqueId(), karmaService.getValueKillVillager());
            }
            return;
        }

        // Evil mob killed by player
        if (karmaService.isEvilMob(entity.getType())) {
            Player killer = entity.getKiller();
            if (killer == null && entity.getLastDamageCause() instanceof EntityDamageByEntityEvent edbe) {
                killer = resolveDamagerPlayer(edbe.getDamager());
            }
            if (killer != null) {
                karmaService.addKarma(killer.getUniqueId(), karmaService.getValueKillEvilMob());
            }
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getTarget() instanceof Player player)) return;
        // Only block targeting if attacker is an "evil" mob and player is EVIL alignment
        if (karmaService.isEvilMob(event.getEntity().getType())) {
            Alignment alignment = karmaService.getAlignment(player.getUniqueId());
            if (alignment == Alignment.EVIL) {
                event.setTarget(null);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        // Re-apply health cap via next scheduler run and update displays immediately
        bossBarManager.updatePlayer(player.getUniqueId());
        nameTagManager.assignPlayerToAlignmentTeam(player.getUniqueId());
    }

    private Player resolveDamagerPlayer(Entity potentialDamager) {
        if (potentialDamager instanceof Player p) return p;
        if (potentialDamager instanceof Projectile projectile) {
            ProjectileSource source = projectile.getShooter();
            if (source instanceof Player p) return p;
        }
        return null;
    }
}


