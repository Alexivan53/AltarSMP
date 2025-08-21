package org.AltarSMP.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.scheduler.BukkitRunnable;
import org.AltarSMP.items.CustomItems;
import org.AltarSMP.AltarSMP;

import java.util.HashMap;
import java.util.UUID;

public class GiveCraftingMaterialsCommand implements CommandExecutor {

    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private final long cooldownTime = 60;
    private final AltarSMP plugin;

    public GiveCraftingMaterialsCommand(AltarSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();

        if (cooldowns.containsKey(playerId)) {
            long timeLeft = ((cooldowns.get(playerId) / 1000) + cooldownTime) - (System.currentTimeMillis() / 1000);
            if (timeLeft > 0) {
                player.sendMessage(ChatColor.RED + "You must wait " + timeLeft + " seconds before using this command again.");
                return true;
            }
        }

        giveMaterials(player);
        
        player.sendMessage(ChatColor.GREEN + "You have received all the materials needed to craft the Bone Blade!");
        player.sendMessage(ChatColor.YELLOW + "Note: You'll need to craft the Weapon Handle first using the recipe!");

        cooldowns.put(playerId, System.currentTimeMillis());

        showCooldownBossBar(player);

        return true;
    }

    private void giveMaterials(Player player) {
        player.getInventory().addItem(
            new ItemStack(Material.BONE_BLOCK, 64),
            new ItemStack(Material.BONE_BLOCK, 64),
            CustomItems.createWeaponHandle(),             
            createWardenHeart(),            
            new ItemStack(Material.IRON_BLOCK, 64),      
            new ItemStack(Material.COPPER_BLOCK, 64),   
            new ItemStack(Material.SKELETON_SKULL, 6),   
            new ItemStack(Material.WITHER_SKELETON_SKULL, 6), 
            CustomItems.createPlayerHead(),               
            CustomItems.createPlayerHead(),
            CustomItems.createPlayerHead()
        );

        player.getInventory().addItem(createTestWardenHeart());
    }
    
    private ItemStack createTestWardenHeart() {
        ItemStack testHeart = new ItemStack(Material.DIAMOND);
        ItemMeta meta = testHeart.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Test Warden Heart");
            meta.setCustomModelData(1);
            meta.setLore(java.util.Arrays.asList(
                ChatColor.YELLOW + "Test item with Custom Model Data: 1",
                ChatColor.GRAY + "This should help debug the issue"
            ));
            testHeart.setItemMeta(meta);
            plugin.getLogger().info("Created Test Warden Heart with Custom Model Data: " + meta.getCustomModelData());
        }
        
        return testHeart;
    }

    private ItemStack createWardenHeart() {
        ItemStack wardenHeart = new ItemStack(Material.EMERALD);
        ItemMeta meta = wardenHeart.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Warden Heart");

            meta.setCustomModelData(10001);

            meta.setLore(java.util.Arrays.asList(
                ChatColor.GRAY + "A powerful heart from a fallen Warden",
                ChatColor.GRAY + "Used in crafting powerful weapons",
                ChatColor.DARK_PURPLE + "Custom Model Data: 10001"
            ));

            NamespacedKey key = new NamespacedKey(plugin, "warden_heart");
            meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
            
            wardenHeart.setItemMeta(meta);

            plugin.getLogger().info("Created Warden Heart with Custom Model Data: " + meta.getCustomModelData());
        } else {
            plugin.getLogger().warning("Failed to get ItemMeta for Warden Heart!");
        }
        
        return wardenHeart;
    }

    private void showCooldownBossBar(Player player) {
        BossBar bossBar = Bukkit.createBossBar(
            "§6Crafting Materials §7Cooldown: §e" + cooldownTime + "s",
            BarColor.RED,
            BarStyle.SOLID
        );
        
        bossBar.addPlayer(player);
        bossBar.setVisible(true);
        bossBar.setProgress(1.0);

        new BukkitRunnable() {
            int timeLeft = (int) cooldownTime;

            @Override
            public void run() {
                if (timeLeft <= 0) {
                    bossBar.setVisible(false);
                    bossBar.removeAll();
                    this.cancel();
                } else {
                    bossBar.setTitle("§6Crafting Materials §7Cooldown: §e" + timeLeft + "s");
                    bossBar.setProgress((double) timeLeft / cooldownTime);
                    timeLeft--;
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }
}
