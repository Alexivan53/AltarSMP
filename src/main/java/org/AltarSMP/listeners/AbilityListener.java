package org.AltarSMP.listeners;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.AltarSMP.AltarSMP;
import org.AltarSMP.cooldowns.AbilityCooldownManager;
import org.AltarSMP.items.CustomItems;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AbilityListener implements Listener {

    private final AbilityCooldownManager cooldownManager;
    private final Map<UUID, Long> lastKillTime = new HashMap<>();
    private final Map<UUID, Integer> killCounts = new HashMap<>();
    private final Map<UUID, EntityType> lastKilledMob = new HashMap<>();
    private final Map<UUID, Boolean> currentDisguise = new HashMap<>();
    private final Map<UUID, BukkitRunnable> boneCageRunnables = new HashMap<>();

    public AbilityListener(AltarSMP plugin) {
        this.cooldownManager = new AbilityCooldownManager(plugin);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player)) {
            return;
        }

        Player target = (Player) event.getRightClicked();
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (isBoneBlade(item) && player.isSneaking()) {
            event.setCancelled(true);
            
            if (cooldownManager.isOnCooldown(player, "Bone Cage")) {
                long remaining = cooldownManager.getRemainingCooldown(player, "Bone Cage");
                player.sendMessage(ChatColor.RED + "Bone Cage is on cooldown for " + remaining + " more seconds!");
                return;
            }
            
            performBoneCage(player, target);
            cooldownManager.setCooldown(player, "Bone Cage", 15);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (isBoneBlade(item) && (event.getAction().toString().contains("RIGHT_CLICK"))) {
            if (!player.isSneaking()) {
                event.setCancelled(true);
                
                if (cooldownManager.isOnCooldown(player, "Skeletal Leap")) {
                    long remaining = cooldownManager.getRemainingCooldown(player, "Skeletal Leap");
                    player.sendMessage(ChatColor.RED + "Skeletal Leap is on cooldown for " + remaining + " more seconds!");
                    return;
                }
                
                performSkeletalLeap(player);
                cooldownManager.setCooldown(player, "Skeletal Leap", 8);
            }
        }
        
        if (CustomItems.isBloodlust(item)) {
            int kills = CustomItems.getBloodlustKillCount(item);
            
            if (kills >= 3 && event.getAction().toString().contains("RIGHT_CLICK")) {
                if (!player.isSneaking()) {
                    createBloodTrail(player);
                    event.setCancelled(true);
                }
            }
            
            if (kills >= 5 && event.getAction().toString().contains("RIGHT_CLICK")) {
                if (player.isSneaking()) {
                    performBloodHook(player);
                    event.setCancelled(true);
                }
            }
        }
        
        if (CustomItems.isWandOfIllusion(item) && event.getAction().toString().contains("RIGHT_CLICK")) {
            event.setCancelled(true);
            performIllusionTransformation(player);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player victim = (Player) event.getEntity();
        Player killer = victim.getKiller();
        
        if (killer == null) {
            return;
        }
        
        ItemStack mainHand = killer.getInventory().getItemInMainHand();
        if (!CustomItems.isBloodlust(mainHand)) {
            return;
        }
        
        int currentKills = CustomItems.getBloodlustKillCount(mainHand);
        int newKills = currentKills + 1;
        CustomItems.setBloodlustKillCount(mainHand, newKills);
        
        lastKillTime.put(victim.getUniqueId(), System.currentTimeMillis());
        killCounts.put(victim.getUniqueId(), newKills);
        
        applyKillAbilities(killer, newKills);
        
        killer.getWorld().spawnParticle(Particle.DUST, killer.getLocation(), 50, 0.5, 0.5, 0.5, new Particle.DustOptions(Color.RED, 2));
        killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.5f);
        
        killer.sendMessage(ChatColor.DARK_RED + "Bloodlust: " + ChatColor.RED + "Kill count increased to " + newKills + "!");
    }

    @EventHandler
    public void onEntityDeathForIllusion(EntityDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player) {
            Player player = event.getEntity().getKiller();
            EntityType killedMob = event.getEntityType();
            
            lastKilledMob.put(player.getUniqueId(), killedMob);
            
            player.getWorld().spawnParticle(Particle.ENCHANT, event.getEntity().getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);
            
            player.sendMessage(ChatColor.LIGHT_PURPLE + "Wand of Illusion: " + ChatColor.GRAY + "You can now transform into a " + 
                            ChatColor.YELLOW + killedMob.toString().toLowerCase().replace("_", " ") + ChatColor.GRAY + "!");
        }
    }

    @EventHandler
    public void onPlayerItemDamage(PlayerItemDamageEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (!CustomItems.isBloodlust(item)) {
            return;
        }
        
        int kills = CustomItems.getBloodlustKillCount(item);
        
        if (kills >= 0) {
            if (Math.random() < 0.15) {
                for (Player nearby : player.getWorld().getPlayers()) {
                    if (nearby.getLocation().distance(player.getLocation()) <= 3.0 && nearby != player) {
                        applyBleedingEffect(nearby);
                        player.sendMessage(ChatColor.DARK_RED + "Bloodlust: " + ChatColor.RED + "Infection attack triggered!");
                    }
                }
            }
        }
    }

    private boolean isBoneBlade(ItemStack item) {
        if (item == null || item.getType() != Material.BONE) {
            return false;
        }
        
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }
        
        return item.getItemMeta().getDisplayName().equals(ChatColor.WHITE + "Bone Blade");
    }

    private void performBoneCage(Player caster, Player target) {
        if (target.hasMetadata("bone_cage_frozen")) {
            caster.sendMessage(ChatColor.RED + "That player is already trapped in a bone cage!");
            return;
        }

        target.setWalkSpeed(0.0f);
        target.setFlySpeed(0.0f);
        target.setMetadata("bone_cage_frozen", new org.bukkit.metadata.FixedMetadataValue(AltarSMP.getPlugin(AltarSMP.class), true));

        createBoneCage(target);

        caster.sendMessage(ChatColor.GREEN + "You trapped " + target.getName() + " in a bone cage!");
        target.sendMessage(ChatColor.RED + "You have been trapped in a bone cage by " + caster.getName() + "!");

        target.sendTitle(ChatColor.RED + "STUNNED", ChatColor.YELLOW + "You are trapped in a bone cage!", 10, 70, 20);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (target.isOnline()) {
                    target.setWalkSpeed(0.2f);
                    target.setFlySpeed(0.1f);
                    target.removeMetadata("bone_cage_frozen", AltarSMP.getPlugin(AltarSMP.class));
                    target.sendMessage(ChatColor.GREEN + "You are free from the bone cage!");
                    
                    removeBoneCage(target);
                }
            }
        }.runTaskLater(AltarSMP.getPlugin(AltarSMP.class), 80L);
    }

    private void createBoneCage(Player target) {
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!target.isOnline() || !target.hasMetadata("bone_cage_frozen")) {
                    cancel();
                    return;
                }

                Location center = target.getLocation().clone().add(0, 0, 0);
                for (int x = -1; x <= 1; x++) {
                    for (int y = 0; y <= 2; y++) {
                        for (int z = -1; z <= 1; z++) {
                            if ((x == 0 && y == 0) || (x == 0 && y == 1)) {
                                continue;
                            }
                            if (Math.abs(x) + Math.abs(y - 1) + Math.abs(z) >= 2) {
                                Location loc = center.clone().add(x + 0.5, y, z + 0.5);
                                target.getWorld().spawnParticle(Particle.BLOCK, loc, 4, 0.02, 0.02, 0.02, Material.BONE_BLOCK.createBlockData());
                            }
                        }
                    }
                }
            }
        };
        task.runTaskTimer(AltarSMP.getPlugin(AltarSMP.class), 0L, 2L);
        boneCageRunnables.put(target.getUniqueId(), task);
    }

    private void removeBoneCage(Player target) {
        BukkitRunnable task = boneCageRunnables.remove(target.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    private void performSkeletalLeap(Player player) {
        Vector direction = player.getLocation().getDirection();
        Vector leapVelocity = direction.multiply(1.5).setY(1.2);
        
        player.setVelocity(leapVelocity);
        player.sendMessage(ChatColor.GREEN + "You performed a Skeletal Leap!");
        
        player.getWorld().spawnParticle(Particle.SMOKE, player.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
    }


    private void performIllusionTransformation(Player player) {
        EntityType lastKilled = lastKilledMob.get(player.getUniqueId());
        
        if (lastKilled == null) {
            player.sendMessage(ChatColor.LIGHT_PURPLE + "Wand of Illusion: " + ChatColor.RED + "You haven't killed any mobs recently!");
            return;
        }
        
        if (currentDisguise.containsKey(player.getUniqueId())) {
            removeDisguise(player);
            player.sendMessage(ChatColor.LIGHT_PURPLE + "Wand of Illusion: " + ChatColor.GREEN + "Transformation removed!");
        } else {
            applyDisguise(player, lastKilled);
            player.sendMessage(ChatColor.LIGHT_PURPLE + "Wand of Illusion: " + ChatColor.GREEN + "You transformed into a " + 
                            ChatColor.YELLOW + lastKilled.toString().toLowerCase().replace("_", " ") + ChatColor.GREEN + "!");
        }
    }

    private void applyDisguise(Player player, EntityType entityType) {
        try {
            applyLibsDisguises(player, entityType);
            currentDisguise.put(player.getUniqueId(), true);
            
            player.getWorld().spawnParticle(Particle.ENCHANT, player.getLocation(), 50, 0.5, 0.5, 0.5, 0.1);
            player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.0f, 1.0f);
            
            player.setDisplayName(ChatColor.LIGHT_PURPLE + "[" + entityType.toString().toLowerCase().replace("_", " ") + "] " + player.getName());
            
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error applying disguise: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void removeDisguise(Player player) {
        Boolean hadDisguise = currentDisguise.remove(player.getUniqueId());
        if (hadDisguise != null && hadDisguise) {
            try {
                removeLibsDisguises(player);
                
                player.setDisplayName(player.getName());
                
                player.getWorld().spawnParticle(Particle.SMOKE, player.getLocation(), 30, 0.5, 0.5, 0.5, 0.1);
                player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.0f, 0.5f);
                
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Error removing disguise: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void applyLibsDisguises(Player player, EntityType entityType) throws Exception {
        Class<?> mobDisguiseClass = Class.forName("me.libraryaddict.disguises.disguisetypes.MobDisguise");
        Class<?> disguiseApiClass = Class.forName("me.libraryaddict.disguises.api.DisguiseAPI");

        Object mobDisguise = mobDisguiseClass.getConstructor(EntityType.class).newInstance(entityType);
        disguiseApiClass.getMethod("disguiseToAll", Entity.class, Class.forName("me.libraryaddict.disguises.disguisetypes.Disguise")).invoke(null, player, mobDisguise);
    }

    private void removeLibsDisguises(Player player) throws Exception {
        Class<?> disguiseApiClass = Class.forName("me.libraryaddict.disguises.api.DisguiseAPI");
        disguiseApiClass.getMethod("undisguiseToAll", Entity.class).invoke(null, player);
    }
    
    private void applyKillAbilities(Player player, int kills) {
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.STRENGTH);
        
        switch (kills) {
            case 1:
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
                player.sendMessage(ChatColor.DARK_RED + "Bloodlust: " + ChatColor.GREEN + "Permanent Speed II unlocked!");
                break;
                
            case 2:
                player.sendMessage(ChatColor.DARK_RED + "Bloodlust: " + ChatColor.GREEN + "Blood Tracker unlocked! You can track killed players for 30 minutes.");
                break;
                
            case 3:
                player.sendMessage(ChatColor.DARK_RED + "Bloodlust: " + ChatColor.GREEN + "Blood Trail unlocked! Right-click to create blood pools with buffs.");
                break;
                
            case 4:
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 0, false, false));
                player.sendMessage(ChatColor.DARK_RED + "Bloodlust: " + ChatColor.GREEN + "Permanent Strength I unlocked!");
                break;
                
            case 5:
                player.sendMessage(ChatColor.DARK_RED + "Bloodlust: " + ChatColor.GREEN + "Blood Hook unlocked! Shift + Right-click to hook enemies.");
                break;
        }
    }
    
    private void createBloodTrail(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 0, false, false));

        player.sendMessage(ChatColor.DARK_RED + "Bloodlust: " + ChatColor.GREEN + "Blood Trail activated! You feel empowered!");
        player.playSound(player.getLocation(), Sound.BLOCK_GRAVEL_PLACE, 1.0f, 0.5f);

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!player.isOnline() || ticks >= 200) {
                    cancel();
                    return;
                }
                Location loc = player.getLocation().clone().add(0, 0.1, 0);
                player.getWorld().spawnParticle(Particle.DUST, loc, 14, 0.25, 0.01, 0.25, new Particle.DustOptions(Color.fromRGB(139, 0, 0), 1.2F));
                ticks++;
            }
        }.runTaskTimer(AltarSMP.getPlugin(AltarSMP.class), 0L, 1L);
    }
    
    private void performBloodHook(Player player) {
        Player nearest = null;
        double minDistance = 20.0;
        
        for (Player target : player.getWorld().getPlayers()) {
            if (target != player && !target.isDead()) {
                double distance = player.getLocation().distance(target.getLocation());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = target;
                }
            }
        }
        
        if (nearest == null) {
            player.sendMessage(ChatColor.DARK_RED + "Bloodlust: " + ChatColor.RED + "No players nearby to hook!");
            return;
        }

        Vector direction = player.getLocation().toVector().subtract(nearest.getLocation().toVector()).normalize();
        nearest.setVelocity(direction.multiply(2.0).setY(1.0));

        player.getWorld().spawnParticle(Particle.DUST, nearest.getLocation(), 50, 0.5, 0.5, 0.5, new Particle.DustOptions(Color.RED, 2));
        player.playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_SPLASH, 1.0f, 0.5f);
        
        player.sendMessage(ChatColor.DARK_RED + "Bloodlust: " + ChatColor.GREEN + "Hooked " + nearest.getName() + "!");
        nearest.sendMessage(ChatColor.DARK_RED + "Bloodlust: " + ChatColor.RED + "You've been hooked by " + player.getName() + "!");
    }
    
    private void applyBleedingEffect(Player target) {
        target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0, false, false));
        target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 60, 0, false, false));

        target.getWorld().spawnParticle(Particle.DUST, target.getLocation(), 30, 0.5, 0.5, 0.5, new Particle.DustOptions(Color.RED, 1));
        target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1.5f);
        
        target.sendMessage(ChatColor.DARK_RED + "Bloodlust: " + ChatColor.RED + "You're bleeding from the infection attack!");
    }

    public boolean canTrackPlayer(UUID victimId) {
        if (!lastKillTime.containsKey(victimId)) {
            return false;
        }
        
        long killTime = lastKillTime.get(victimId);
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - killTime;

        return timeDiff <= 1800000;
    }

    public int getPlayerKillCount(UUID victimId) {
        return killCounts.getOrDefault(victimId, 0);
    }
}
