package org.AltarSMP.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.entity.Player;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.AltarSMP.AltarSMP;

import java.util.Arrays;
import java.util.List;

public class CustomItems {

    public static ItemStack createWeaponHandle() {
        ItemStack handle = new ItemStack(Material.STICK);
        ItemMeta meta = handle.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Weapon Handle");
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "A sturdy handle crafted from",
                ChatColor.GRAY + "iron and breeze rods.",
                ChatColor.YELLOW + "Used in weapon crafting."
            ));
            handle.setItemMeta(meta);
        }
        return handle;
    }

    public static ItemStack createWardensHeart() {
        ItemStack heart = new ItemStack(Material.HEART_OF_THE_SEA);
        ItemMeta meta = heart.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_PURPLE + "Warden's Heart");
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "A powerful essence extracted",
                ChatColor.GRAY + "from the depths of the ancient city.",
                ChatColor.RED + "Rare drop from Wardens.",
                ChatColor.YELLOW + "Used in powerful weapon crafting."
            ));
            meta.setCustomModelData(1001);
            heart.setItemMeta(meta);
        }
        return heart;
    }

    public static ItemStack createBoneBlade() {
        ItemStack boneBlade = new ItemStack(Material.BONE);
        ItemMeta meta = boneBlade.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.WHITE + "Bone Blade");
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Abilities:",
                ChatColor.YELLOW + "Bone Cage: Shift + Right-click on a player",
                ChatColor.YELLOW + "to trap them in a bone cage for 4 seconds!",
                ChatColor.YELLOW + "Surrounded by mini bone blocks!",
                ChatColor.YELLOW + "",
                ChatColor.YELLOW + "Skeletal Leap: Right-click to perform",
                ChatColor.YELLOW + "a powerful leap forward and upward!",
                ChatColor.GRAY + "",
                ChatColor.GOLD + "Legendary weapon crafted from rare materials."
            ));
            boneBlade.setItemMeta(meta);
        }
        return boneBlade;
    }

    public static ItemStack createPlayerHead() {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = head.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "Player Head");
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "A preserved player head.",
                ChatColor.YELLOW + "Used in powerful weapon crafting."
            ));
            head.setItemMeta(meta);
        }
        return head;
    }

    public static ItemStack createIllusionCore() {
        ItemStack core = new ItemStack(Material.AMETHYST_SHARD);
        ItemMeta meta = core.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Illusion Core");
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "A condensed shard of illusory energy.",
                ChatColor.YELLOW + "Required to forge the Wand of Illusion."
            ));
            meta.setCustomModelData(2001);
            NamespacedKey key = new NamespacedKey(AltarSMP.getPlugin(AltarSMP.class), "illusion_core");
            meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
            core.setItemMeta(meta);
        }
        return core;
    }


    public static ItemStack createBloodlust() {
        ItemStack bloodlust = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = bloodlust.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§4§lBloodlust");

            meta.setLore(Arrays.asList(
                "§7§oA powerful blade made with solid blood.",
                "§7§oThe more you kill with it, the stronger you become.",
                "§e§l!! §eShift-Left-Click §7to view kill count.",
                "",
                "§cInfection §7(0+ Kills)",
                "§7 15% chance to bleed an enemy on hit. §715 second cooldown.",
                "",
                "§cSpeed II §7(1+ Kill)",
                "§7 Gain permanent §bSpeed II §7when holding Bloodlust.",
                "",
                "§cBlood Tracker §7(2+ Kills)",
                "§7 Track the blood of nearby players when holding Bloodlust.",
                "§7 §oOnly you can see the particles.",
                "",
                "§cBlood Trail §7(3+ Kills)",
                "§7 Submerge yourself in a puddle of blood. §761 second cooldown.",
                "",
                "§cStrength I §7(4+ Kills)",
                "§7 Gain permanent §cStrength I §7when holding Bloodlust.",
                "",
                "§cBlood Hook §7(5+ Kills)",
                "§7 Throw a blood chain in front of you, pulling anything it",
                "§7 touches towards you. §730 second cooldown."
            ));

            meta.addEnchant(Enchantment.UNBREAKING, 1, true);

            NamespacedKey key = new NamespacedKey(AltarSMP.getPlugin(AltarSMP.class), "bloodlust");
            meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);

            NamespacedKey killKey = new NamespacedKey(AltarSMP.getPlugin(AltarSMP.class), "bloodlust_kills");
            meta.getPersistentDataContainer().set(killKey, PersistentDataType.INTEGER, 0);

            bloodlust.setItemMeta(meta);
        }

        return bloodlust;
    }

    public static boolean isBloodlust(ItemStack item) {
        if (item == null || item.getType() != Material.DIAMOND_SWORD) {
            return false;
        }

        if (!item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(AltarSMP.getPlugin(AltarSMP.class), "bloodlust");
        return meta.getPersistentDataContainer().has(key, PersistentDataType.BOOLEAN);
    }

    public static int getBloodlustKillCount(ItemStack item) {
        if (!isBloodlust(item)) {
            return 0;
        }

        ItemMeta meta = item.getItemMeta();
        NamespacedKey killKey = new NamespacedKey(AltarSMP.getPlugin(AltarSMP.class), "bloodlust_kills");
        return meta.getPersistentDataContainer().getOrDefault(killKey, PersistentDataType.INTEGER, 0);
    }

    public static void setBloodlustKillCount(ItemStack item, int kills) {
        if (!isBloodlust(item)) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        NamespacedKey killKey = new NamespacedKey(AltarSMP.getPlugin(AltarSMP.class), "bloodlust_kills");
        meta.getPersistentDataContainer().set(killKey, PersistentDataType.INTEGER, kills);

        updateBloodlustLore(meta, kills);

        item.setItemMeta(meta);
    }

    private static void updateBloodlustLore(ItemMeta meta, int killsIgnored) {
        meta.setLore(Arrays.asList(
            "§7§oA powerful blade made with solid blood.",
            "§7§oThe more you kill with it, the stronger you become.",
            "§e§l!! §eShift-Left-Click §7to view kill count.",
            "",
            "§cInfection §7(0+ Kills)",
            "§7 15% chance to bleed an enemy on hit. §715 second cooldown.",
            "",
            "§cSpeed II §7(1+ Kill)",
            "§7 Gain permanent §bSpeed II §7when holding Bloodlust.",
            "",
            "§cBlood Tracker §7(2+ Kills)",
            "§7 Track the blood of nearby players when holding Bloodlust.",
            "§7 §oOnly you can see the particles.",
            "",
            "§cBlood Trail §7(3+ Kills)",
            "§7 Submerge yourself in a puddle of blood. §761 second cooldown.",
            "",
            "§cStrength I §7(4+ Kills)",
            "§7 Gain permanent §cStrength I §7when holding Bloodlust.",
            "",
            "§cBlood Hook §7(5+ Kills)",
            "§7 Throw a blood chain in front of you, pulling anything it",
            "§7 touches towards you. §730 second cooldown."
        ));
    }

    
    public static ItemStack createWandOfIllusion() {
        ItemStack wand = new ItemStack(Material.STICK);
        ItemMeta meta = wand.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Wand of Illusion");

            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "A mystical wand that allows you to",
                ChatColor.GRAY + "transform into mobs you've recently killed.",
                "",
                ChatColor.LIGHT_PURPLE + "Right-click to transform into",
                ChatColor.LIGHT_PURPLE + "the last mob you killed!"
            ));

            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            NamespacedKey key = new NamespacedKey(AltarSMP.getPlugin(AltarSMP.class), "wand_of_illusion");
            meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);

            wand.setItemMeta(meta);
        }

        return wand;
    }

    public static boolean isWandOfIllusion(ItemStack item) {
        if (item == null || item.getType() != Material.STICK) {
            return false;
        }

        if (!item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(AltarSMP.getPlugin(AltarSMP.class), "wand_of_illusion");
        return meta.getPersistentDataContainer().has(key, PersistentDataType.BOOLEAN);
    }
}
