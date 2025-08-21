package org.AltarSMP.commands;

import org.AltarSMP.AltarSMP;
import org.AltarSMP.items.CustomItems;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.inventory.ItemStack;

public class AltarCommand implements CommandExecutor, TabCompleter {

    private final AltarSMP plugin;

    public AltarCommand(AltarSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("altarsmp.altar")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        if (args.length < 2 || !args[0].equalsIgnoreCase("spawn")) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /altar spawn <bloodlust|boneblade|wandofillusion>");
            return true;
        }
        String type = args[1].toLowerCase();
        switch (type) {
            case "bloodlust":
                spawnAltar(player.getLocation(), type);
                player.sendMessage(ChatColor.GREEN + "Spawned Bloodlust altar.");
                break;
            case "boneblade":
                spawnAltar(player.getLocation(), type);
                player.sendMessage(ChatColor.GREEN + "Spawned Bone Blade altar.");
                break;
            case "wandofillusion":
                spawnAltar(player.getLocation(), type);
                player.sendMessage(ChatColor.GREEN + "Spawned Wand of Illusion altar.");
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown altar type: " + type);
                break;
        }
        return true;
    }

    private void spawnAltar(Location base, String type) {
        Location platform = base.getBlock().getLocation();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Block b = platform.clone().add(x, -1, z).getBlock();
                b.setType(Material.POLISHED_BLACKSTONE);
            }
        }
        Block center = platform.clone().add(0, -1, 0).getBlock();
        center.setType(Material.ENCHANTING_TABLE);
        Location altarCenter = center.getLocation();

        
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ > 20 * 30) {
                    cancel();
                    return;
                }
                base.getWorld().spawnParticle(Particle.ENCHANT, base.clone().add(0, 1, 0), 8, 0.4, 0.6, 0.4, 0.0);
            }
        }.runTaskTimer(plugin, 0L, 10L);

        
        new BukkitRunnable() {
            int life = 20 * 60;
            @Override
            public void run() {
                if (life-- <= 0) { cancel(); return; }
                for (Player p : base.getWorld().getPlayers()) {
                    if (p.getLocation().distance(base) <= 2.0 && p.isSneaking() && p.getInventory().getItemInMainHand().getType() == Material.AIR) {
                        Block target = p.getTargetBlockExact(5);
                        if (target == null || !target.getLocation().equals(altarCenter)) {
                            continue;
                        }
                        if (tryCraft(p, type)) {
                            p.playSound(base, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
                            String crafted = type.equals("boneblade") ? "Bone Blade" : type.equals("bloodlust") ? "Bloodlust" : "Wand of Illusion";
                            Bukkit.broadcastMessage(ChatColor.GOLD + p.getName() + ChatColor.GRAY + " crafted the " + ChatColor.GREEN + crafted + ChatColor.GRAY + "!");
                            cancel();
                            return;
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 10L);
    }

    private boolean tryCraft(Player player, String type) {
        if (type.equals("boneblade")) {
            if (player.getInventory().contains(Material.BONE_BLOCK, 64)
                && player.getInventory().containsAtLeast(CustomItems.createWeaponHandle(), 1)
                && hasWardenHeart(player)
                && player.getInventory().contains(Material.IRON_BLOCK, 64)
                && player.getInventory().contains(Material.COPPER_BLOCK, 64)
                && player.getInventory().contains(Material.SKELETON_SKULL, 6)
                && player.getInventory().contains(Material.WITHER_SKELETON_SKULL, 6)
                && player.getInventory().containsAtLeast(CustomItems.createPlayerHead(), 3)) {
                removeItem(player, Material.BONE_BLOCK, 64);
                removeExactItem(player, CustomItems.createWeaponHandle(), 1);
                removeOneWardenHeart(player);
                removeItem(player, Material.IRON_BLOCK, 64);
                removeItem(player, Material.COPPER_BLOCK, 64);
                removeItem(player, Material.SKELETON_SKULL, 6);
                removeItem(player, Material.WITHER_SKELETON_SKULL, 6);
                removeExactItem(player, CustomItems.createPlayerHead(), 3);
                player.getInventory().addItem(CustomItems.createBoneBlade());
                return true;
            } else {
                player.sendMessage(ChatColor.RED + "Missing materials for Bone Blade.");
            }
        } else if (type.equals("bloodlust")) {
            
            if (player.getInventory().contains(Material.NETHERITE_SCRAP, 4)
                && player.getInventory().contains(Material.DIAMOND, 2)
                && player.getInventory().contains(Material.GHAST_TEAR, 1)) {
                removeItem(player, Material.NETHERITE_SCRAP, 4);
                removeItem(player, Material.DIAMOND, 2);
                removeItem(player, Material.GHAST_TEAR, 1);
                player.getInventory().addItem(CustomItems.createBloodlust());
                return true;
            } else {
                player.sendMessage(ChatColor.RED + "Missing materials for Bloodlust.");
            }
        } else if (type.equals("wandofillusion")) {
            if (player.getInventory().contains(Material.GOLD_BLOCK, 32)
                && hasWardenHeart(player)
                && player.getInventory().contains(Material.TOTEM_OF_UNDYING, 6)
                && player.getInventory().containsAtLeast(CustomItems.createIllusionCore(), 1)
                && player.getInventory().contains(Material.AMETHYST_BLOCK, 64)
                && player.getInventory().contains(Material.ENCHANTED_GOLDEN_APPLE, 2)
                && player.getInventory().containsAtLeast(CustomItems.createPlayerHead(), 2)) {
                removeItem(player, Material.GOLD_BLOCK, 32);
                removeOneWardenHeart(player);
                removeItem(player, Material.TOTEM_OF_UNDYING, 6);
                removeExactItem(player, CustomItems.createIllusionCore(), 1);
                removeItem(player, Material.AMETHYST_BLOCK, 64);
                removeItem(player, Material.ENCHANTED_GOLDEN_APPLE, 2);
                removeExactItem(player, CustomItems.createPlayerHead(), 2);
                player.getInventory().addItem(CustomItems.createWandOfIllusion());
                return true;
            } else {
                player.sendMessage(ChatColor.RED + "Missing materials for Wand of Illusion.");
            }
        }
        return false;
    }

    private ItemStack createWardenHeart() {
        
        ItemStack wardenHeart = new ItemStack(Material.EMERALD);
        org.bukkit.inventory.meta.ItemMeta meta = wardenHeart.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Warden Heart");
            meta.setCustomModelData(10001);
            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "warden_heart");
            meta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.BOOLEAN, true);
            wardenHeart.setItemMeta(meta);
        }
        return wardenHeart;
    }

    private void removeItem(Player player, Material material, int amount) {
        int remaining = amount;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null) continue;
            if (stack.getType() == material) {
                int take = Math.min(remaining, stack.getAmount());
                stack.setAmount(stack.getAmount() - take);
                remaining -= take;
                if (remaining <= 0) break;
            }
        }
    }

    private void removeExactItem(Player player, ItemStack template, int amount) {
        int remaining = amount;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null) continue;
            if (stack.isSimilar(template)) {
                int take = Math.min(remaining, stack.getAmount());
                stack.setAmount(stack.getAmount() - take);
                remaining -= take;
                if (remaining <= 0) break;
            }
        }
    }

    private boolean hasWardenHeart(Player player) {
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null) continue;
            
            org.bukkit.inventory.meta.ItemMeta meta = stack.getItemMeta();
            if (meta == null) continue;
            org.bukkit.persistence.PersistentDataContainer pdc = meta.getPersistentDataContainer();
            if (pdc.has(new org.bukkit.NamespacedKey(plugin, "warden_heart"), org.bukkit.persistence.PersistentDataType.BOOLEAN)) {
                if (Boolean.TRUE.equals(pdc.get(new org.bukkit.NamespacedKey(plugin, "warden_heart"), org.bukkit.persistence.PersistentDataType.BOOLEAN))) {
                    return true;
                }
            }
            if (meta.hasDisplayName() && ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase("Warden's Heart")) {
                return true;
            }
        }
        return false;
    }

    private void removeOneWardenHeart(Player player) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack == null) continue;
            org.bukkit.inventory.meta.ItemMeta meta = stack.getItemMeta();
            if (meta == null) continue;
            org.bukkit.persistence.PersistentDataContainer pdc = meta.getPersistentDataContainer();
            boolean isWardenHeart = false;
            if (pdc.has(new org.bukkit.NamespacedKey(plugin, "warden_heart"), org.bukkit.persistence.PersistentDataType.BOOLEAN)) {
                Boolean v = pdc.get(new org.bukkit.NamespacedKey(plugin, "warden_heart"), org.bukkit.persistence.PersistentDataType.BOOLEAN);
                isWardenHeart = Boolean.TRUE.equals(v);
            }
            if (!isWardenHeart && meta.hasDisplayName() && ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase("Warden's Heart")) {
                isWardenHeart = true;
            }
            if (isWardenHeart) {
                int newAmt = stack.getAmount() - 1;
                if (newAmt <= 0) {
                    player.getInventory().setItem(i, null);
                } else {
                    stack.setAmount(newAmt);
                }
                return;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return Collections.emptyList();
        if (args.length == 1) {
            return Collections.singletonList("spawn");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("spawn")) {
            return Arrays.asList("bloodlust", "boneblade", "wandofillusion");
        }
        return Collections.emptyList();
    }
}


