package org.AltarSMP.listeners;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.World;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;
import org.joml.Quaternionf;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import net.kyori.adventure.text.Component;
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
    private final Map<UUID, BukkitRunnable> disguiseTimers = new HashMap<>();
    private final Map<UUID, java.util.List<Entity>> boneCageDisplays = new HashMap<>();
    private final java.util.Set<UUID> activeBloodTrails = new java.util.HashSet<>();
    private final Map<UUID, BukkitRunnable> bloodTrailTasks = new HashMap<>();
    private final java.util.Set<UUID> bloodFormPlayers = new java.util.HashSet<>();

    public AbilityListener(AltarSMP plugin) {
        this.cooldownManager = new AbilityCooldownManager(plugin);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    boolean hasBloodlust = AbilityListener.this.hasBloodlustInInventory(player);
                    int kills = hasBloodlust ? AbilityListener.this.getHighestBloodlustKills(player) : 0;

                    if (hasBloodlust && kills >= 1) {
                        if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1, false, false));
                        }
                    } else {
                        player.removePotionEffect(PotionEffectType.SPEED);
                    }

                    if (hasBloodlust && kills >= 4) {
                        if (!player.hasPotionEffect(PotionEffectType.STRENGTH)) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 0, false, false));
                        }
                    } else {
                        player.removePotionEffect(PotionEffectType.STRENGTH);
                    }

                    if (hasBloodlust) {
                        Player nearest = AbilityListener.this.findNearestPlayer(player, 40.0);
                        if (nearest != null) {
                            AbilityListener.this.drawDirectionParticles(player.getLocation(), nearest.getLocation(), Color.fromRGB(139, 0, 0));
                        }
                    }
                }
            }
        }.runTaskTimer(AltarSMP.getPlugin(AltarSMP.class), 0L, 20L);
    }

    @EventHandler
    public void onPlayerToggleSneak(org.bukkit.event.player.PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (bloodFormPlayers.contains(player.getUniqueId()) && event.isSneaking()) {
            exitBloodForm(player, true);
        }
    }

    @EventHandler
    public void onPlayerJump(PlayerJumpEvent event) {
        Player player = event.getPlayer();
        if (bloodFormPlayers.contains(player.getUniqueId())) {
            exitBloodForm(player, true);
        }
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
            if (player.isSneaking() && (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
                int kills = CustomItems.getBloodlustKillCount(item);
                if (kills <= 0) {
                    player.sendMessage(ChatColor.DARK_RED + "Bloodlust: " + ChatColor.RED + "Your At UNDEFINED bloodlust kills(s)");
                } else {
                    player.sendMessage(ChatColor.DARK_RED + "Bloodlust: " + ChatColor.GREEN + "You are at " + kills + " bloodlust kill(s)");
                }
                return;
            }
            int kills = CustomItems.getBloodlustKillCount(item);
            
            if (kills >= 3 && event.getAction().toString().contains("RIGHT_CLICK")) {
                if (!player.isSneaking()) {
                    if (cooldownManager.isOnCooldown(player, "Blood Trail")) {
                        long remaining = cooldownManager.getRemainingCooldown(player, "Blood Trail");
                        player.sendMessage(ChatColor.RED + "Blood Trail is on cooldown for " + remaining + " more seconds!");
                        event.setCancelled(true);
                        return;
                    }
                    if (activeBloodTrails.contains(player.getUniqueId())) {
                        player.sendMessage(ChatColor.RED + "Blood Trail is already active!");
                        event.setCancelled(true);
                        return;
                    }
                    createBloodTrail(player);
                    cooldownManager.setCooldown(player, "Blood Trail", 61);
                    event.setCancelled(true);
                }
            }
            
            if (kills >= 5 && event.getAction().toString().contains("RIGHT_CLICK")) {
                if (player.isSneaking()) {
                    if (cooldownManager.isOnCooldown(player, "Blood Hook")) {
                        long remaining = cooldownManager.getRemainingCooldown(player, "Blood Hook");
                        player.sendMessage(ChatColor.RED + "Blood Hook is on cooldown for " + remaining + " more seconds!");
                        event.setCancelled(true);
                        return;
                    }
                    performBloodHook(player);
                    cooldownManager.setCooldown(player, "Blood Hook", 30);
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
        if (!(event.getEntity().getKiller() instanceof Player)) {
            return;
        }
        if (event.getEntity() instanceof Player) {
            return;
        }
        Player player = event.getEntity().getKiller();
        ItemStack used = player.getInventory().getItemInMainHand();
        if (!CustomItems.isWandOfIllusion(used)) {
            return;
        }
        EntityType killedMob = event.getEntityType();
        lastKilledMob.put(player.getUniqueId(), killedMob);
        player.getWorld().spawnParticle(Particle.ENCHANT, event.getEntity().getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Wand of Illusion: " + ChatColor.GRAY + "You can now transform into a " + 
                        ChatColor.YELLOW + killedMob.toString().toLowerCase().replace("_", " ") + ChatColor.GRAY + "!");
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
                if (cooldownManager.isOnCooldown(player, "Infection")) {
                    return;
                }
                for (Player nearby : player.getWorld().getPlayers()) {
                    if (nearby.getLocation().distance(player.getLocation()) <= 3.0 && nearby != player) {
                        applyBleedingEffect(nearby);
                        player.sendMessage(ChatColor.DARK_RED + "Bloodlust: " + ChatColor.RED + "Infection attack triggered!");
                    }
                }
                cooldownManager.setCooldown(player, "Infection", 15);
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
        java.util.List<Entity> displays = new java.util.ArrayList<>();
        Location base = target.getLocation();
        World world = base.getWorld();
        if (world == null) return;
        double radius = 1.2;
        int columns = 12;
        for (int y = 0; y <= 2; y++) {
            for (int i = 0; i < columns; i++) {
                double angle = (2 * Math.PI / columns) * i;
                double x = base.getX() + Math.cos(angle) * radius;
                double z = base.getZ() + Math.sin(angle) * radius;
                Location loc = new Location(world, x, base.getY() + y, z);
                BlockDisplay display = world.spawn(loc, BlockDisplay.class, d -> {
                    d.setBlock(Material.BONE_BLOCK.createBlockData());
                    Transformation tf = new Transformation(new Vector3f(), new Quaternionf(), new Vector3f(0.4f, 0.4f, 0.4f), new Quaternionf());
                    d.setTransformation(tf);
                });
                displays.add(display);
            }
        }
        boneCageDisplays.put(target.getUniqueId(), displays);

        BukkitRunnable particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!target.isOnline() || !target.hasMetadata("bone_cage_frozen")) {
                    cancel();
                    return;
                }
                world.spawnParticle(Particle.DUST, target.getLocation(), 6, 0.6, 0.4, 0.6, new Particle.DustOptions(Color.fromRGB(230,230,230), 1));
            }
        };
        particleTask.runTaskTimer(AltarSMP.getPlugin(AltarSMP.class), 0L, 10L);
        boneCageRunnables.put(target.getUniqueId(), particleTask);
    }

    private void removeBoneCage(Player target) {
        BukkitRunnable task = boneCageRunnables.remove(target.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        java.util.List<Entity> displays = boneCageDisplays.remove(target.getUniqueId());
        if (displays != null) {
            for (Entity e : displays) {
                e.remove();
            }
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
            
            BukkitRunnable prev = disguiseTimers.remove(player.getUniqueId());
            if (prev != null) {
                prev.cancel();
            }
            BukkitRunnable timer = new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline() && currentDisguise.containsKey(player.getUniqueId())) {
                        removeDisguise(player);
                        player.sendMessage(ChatColor.LIGHT_PURPLE + "Wand of Illusion: " + ChatColor.GRAY + "Your disguise has faded.");
                    }
                }
            };
            timer.runTaskLater(AltarSMP.getPlugin(AltarSMP.class), 120L * 20L);
            disguiseTimers.put(player.getUniqueId(), timer);
            
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
                BukkitRunnable t = disguiseTimers.remove(player.getUniqueId());
                if (t != null) {
                    t.cancel();
                }
                
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

        UUID id = player.getUniqueId();
        activeBloodTrails.add(id);
        enterBloodForm(player);
        BukkitRunnable trailTask = new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!player.isOnline() || ticks >= 200) {
                    activeBloodTrails.remove(id);
                    exitBloodForm(player, false);
                    cancel();
                    return;
                }
                Location loc = player.getLocation().clone().add(0, 0.1, 0);
                player.getWorld().spawnParticle(Particle.DUST, loc, 40, 0.5, 0.4, 0.5, new Particle.DustOptions(Color.fromRGB(139, 0, 0), 1.5F));
                player.getWorld().spawnParticle(Particle.DUST, loc, 14, 0.25, 0.01, 0.25, new Particle.DustOptions(Color.fromRGB(139, 0, 0), 1.2F));
                player.sendActionBar(Component.text("Press [JUMP] to Exit."));
                ticks++;
            }
        };
        trailTask.runTaskTimer(AltarSMP.getPlugin(AltarSMP.class), 0L, 1L);
        bloodTrailTasks.put(id, trailTask);
    }

    private void enterBloodForm(Player player) {
        bloodFormPlayers.add(player.getUniqueId());
        try {
            Class<?> mobDisguiseClass = Class.forName("me.libraryaddict.disguises.disguisetypes.MobDisguise");
            Object mobDisguise = mobDisguiseClass.getConstructor(EntityType.class).newInstance(EntityType.SLIME);
            Object watcher = mobDisguiseClass.getMethod("getWatcher").invoke(mobDisguise);
            Class<?> slimeWatcherClass = Class.forName("me.libraryaddict.disguises.watchers.SlimeWatcher");
            if (slimeWatcherClass.isInstance(watcher)) {
                slimeWatcherClass.getMethod("setSize", int.class).invoke(watcher, 1);
            }
            Class<?> disguiseApiClass = Class.forName("me.libraryaddict.disguises.api.DisguiseAPI");
            disguiseApiClass.getMethod("disguiseToAll", Entity.class, Class.forName("me.libraryaddict.disguises.disguisetypes.Disguise")).invoke(null, player, mobDisguise);
        } catch (Exception ignored) {
        }
    }

    private void exitBloodForm(Player player, boolean fromJump) {
        UUID id = player.getUniqueId();
        bloodFormPlayers.remove(id);
        BukkitRunnable task = bloodTrailTasks.remove(id);
        if (task != null) {
            task.cancel();
            activeBloodTrails.remove(id);
        }
        try {
            Class<?> disguiseApiClass = Class.forName("me.libraryaddict.disguises.api.DisguiseAPI");
            disguiseApiClass.getMethod("undisguiseToAll", Entity.class).invoke(null, player);
        } catch (Exception ignored) {
        }
        player.removePotionEffect(PotionEffectType.REGENERATION);
        player.removePotionEffect(PotionEffectType.RESISTANCE);
        if (fromJump) {
            player.sendMessage(ChatColor.DARK_RED + "Bloodlust: " + ChatColor.RED + "Blood Trail ended.");
        }
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

    private boolean hasBloodlustInInventory(Player player) {
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && CustomItems.isBloodlust(stack)) {
                return true;
            }
        }
        return false;
    }

    private int getHighestBloodlustKills(Player player) {
        int max = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && CustomItems.isBloodlust(stack)) {
                max = Math.max(max, CustomItems.getBloodlustKillCount(stack));
            }
        }
        return max;
    }

    private Player findNearestPlayer(Player source, double radius) {
        Player nearest = null;
        double min = radius;
        for (Player p : source.getWorld().getPlayers()) {
            if (p == source || p.isDead()) continue;
            double d = p.getLocation().distance(source.getLocation());
            if (d <= min) {
                min = d;
                nearest = p;
            }
        }
        return nearest;
    }

    private void drawDirectionParticles(Location from, Location to, Color color) {
        Vector dir = to.toVector().subtract(from.toVector());
        double length = dir.length();
        if (length < 1e-6) return;
        Vector step = dir.normalize().multiply(0.8);
        Location cursor = from.clone().add(0, 1.5, 0);
        int points = (int) Math.min(20, Math.ceil(length / 0.8));
        for (int i = 0; i < points; i++) {
            cursor.getWorld().spawnParticle(Particle.DUST, cursor, 3, 0.02, 0.02, 0.02, new Particle.DustOptions(color, 1));
            cursor.add(step);
        }
    }
}
