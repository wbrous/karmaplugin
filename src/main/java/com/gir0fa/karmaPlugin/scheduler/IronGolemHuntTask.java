package com.gir0fa.karmaPlugin.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import com.gir0fa.karmaPlugin.model.Alignment;
import com.gir0fa.karmaPlugin.service.KarmaService;

public class IronGolemHuntTask extends BukkitRunnable {

    private final KarmaService karmaService;

    public IronGolemHuntTask(KarmaService karmaService) {
        this.karmaService = karmaService;
    }

    @Override
    public void run() {
        // Make iron golems hunt evil players every 2 seconds
        for (Player player : Bukkit.getOnlinePlayers()) {
            Alignment alignment = karmaService.getAlignment(player.getUniqueId());
            if (alignment != Alignment.EVIL) continue;

            // Find iron golems within 16 blocks of the evil player
            for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), 16, 16, 16)) {
                if (entity instanceof IronGolem golem) {
                    // Check if golem is not already targeting someone or is targeting a non-evil player
                    LivingEntity currentTarget = golem.getTarget();
                    if (currentTarget == null || 
                        !(currentTarget instanceof Player targetPlayer) || 
                        karmaService.getAlignment(targetPlayer.getUniqueId()) != Alignment.EVIL) {
                        golem.setTarget(player);
                    }
                }
            }
        }
    }
}
