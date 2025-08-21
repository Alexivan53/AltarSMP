package org.AltarSMP.cooldowns;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.AltarSMP.AltarSMP;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AbilityCooldownManager {
    
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private final Map<UUID, Map<String, BossBar>> bossBars = new HashMap<>();
    private final AltarSMP plugin;
    
    public AbilityCooldownManager(AltarSMP plugin) {
        this.plugin = plugin;
    }
    
    public boolean isOnCooldown(Player player, String abilityName) {
        UUID playerId = player.getUniqueId();
        if (!cooldowns.containsKey(playerId)) {
            return false;
        }
        
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (!playerCooldowns.containsKey(abilityName)) {
            return false;
        }
        
        long cooldownEnd = playerCooldowns.get(abilityName);
        return System.currentTimeMillis() < cooldownEnd;
    }
    
    public void setCooldown(Player player, String abilityName, long cooldownSeconds) {
        UUID playerId = player.getUniqueId();

        cooldowns.putIfAbsent(playerId, new HashMap<>());
        bossBars.putIfAbsent(playerId, new HashMap<>());

        long cooldownEnd = System.currentTimeMillis() + (cooldownSeconds * 1000);
        cooldowns.get(playerId).put(abilityName, cooldownEnd);

        showBossBar(player, abilityName, cooldownSeconds);
    }
    
    private void showBossBar(Player player, String abilityName, long cooldownSeconds) {
        UUID playerId = player.getUniqueId();

        if (bossBars.get(playerId).containsKey(abilityName)) {
            bossBars.get(playerId).get(abilityName).removeAll();
        }

        BossBar bossBar = Bukkit.createBossBar(
            "§6" + abilityName + " §7Cooldown",
            BarColor.RED,
            BarStyle.SOLID
        );
        
        bossBar.addPlayer(player);
        bossBar.setProgress(1.0);
        bossBars.get(playerId).put(abilityName, bossBar);

        new BukkitRunnable() {
            double progress = 1.0;
            long startTime = System.currentTimeMillis();
            long endTime = startTime + (cooldownSeconds * 1000);
            
            @Override
            public void run() {
                if (!player.isOnline() || System.currentTimeMillis() >= endTime) {
                    
                    if (bossBars.get(playerId).containsKey(abilityName)) {
                        bossBars.get(playerId).get(abilityName).removeAll();
                        bossBars.get(playerId).remove(abilityName);
                    }
                    if (cooldowns.get(playerId).containsKey(abilityName)) {
                        cooldowns.get(playerId).remove(abilityName);
                    }
                    this.cancel();
                    return;
                }

                long remainingTime = endTime - System.currentTimeMillis();
                progress = (double) remainingTime / (cooldownSeconds * 1000);
                bossBar.setProgress(Math.max(0.0, progress));

                int remainingSeconds = (int) (remainingTime / 1000);
                bossBar.setTitle("§6" + abilityName + " §7Cooldown: §e" + remainingSeconds + "s");
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    public void removeCooldown(Player player, String abilityName) {
        UUID playerId = player.getUniqueId();
        
        if (cooldowns.containsKey(playerId)) {
            cooldowns.get(playerId).remove(abilityName);
        }
        
        if (bossBars.containsKey(playerId) && bossBars.get(playerId).containsKey(abilityName)) {
            bossBars.get(playerId).get(abilityName).removeAll();
            bossBars.get(playerId).remove(abilityName);
        }
    }
    
    public void clearAllCooldowns(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (bossBars.containsKey(playerId)) {
            bossBars.get(playerId).values().forEach(BossBar::removeAll);
            bossBars.get(playerId).clear();
        }
        
        cooldowns.remove(playerId);
    }
    
    public long getRemainingCooldown(Player player, String abilityName) {
        UUID playerId = player.getUniqueId();
        if (!cooldowns.containsKey(playerId) || !cooldowns.get(playerId).containsKey(abilityName)) {
            return 0;
        }
        
        long remaining = cooldowns.get(playerId).get(abilityName) - System.currentTimeMillis();
        return Math.max(0, remaining / 1000);
    }
}
