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


    public static ItemStack createBloodlust() {
        ItemStack bloodlust = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = bloodlust.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_RED + "Bloodlust");

            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "A cursed sword that grows stronger",
                ChatColor.GRAY + "with each kill...",
                "",
                ChatColor.RED + "0 Kills:",
                ChatColor.GRAY + "• Infection Attack (15% bleed chance)",
                "",
                ChatColor.RED + "1 Kill:",
                ChatColor.GRAY + "• Permanent Speed II",
                "",
                ChatColor.RED + "2 Kills:",
                ChatColor.GRAY + "• Blood Tracker (30 min tracking)",
                "",
                ChatColor.RED + "3 Kills:",
                ChatColor.GRAY + "• Blood Trail (Right-click for buffs)",
                "",
                ChatColor.RED + "4 Kills:",
                ChatColor.GRAY + "• Permanent Strength I",
                "",
                ChatColor.RED + "5 Kills:",
                ChatColor.GRAY + "• Blood Hook (Shift + Right-click)",
                "",
                ChatColor.DARK_PURPLE + "Kill Count: 0"
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

    private static void updateBloodlustLore(ItemMeta meta, int kills) {
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "A cursed sword that grows stronger",
            ChatColor.GRAY + "with each kill...",
            "",
            ChatColor.RED + "0 Kills:",
            ChatColor.GRAY + "• Infection Attack (15% bleed chance)",
            "",
            ChatColor.RED + "1 Kill:",
            ChatColor.GRAY + "• Permanent Speed II",
            "",
            ChatColor.RED + "2 Kills:",
            ChatColor.GRAY + "• Blood Tracker (30 min tracking)",
            "",
            ChatColor.RED + "3 Kills:",
            ChatColor.GRAY + "• Blood Trail (Right-click for buffs)",
            "",
            ChatColor.RED + "4 Kills:",
            ChatColor.GRAY + "• Permanent Strength I",
            "",
            ChatColor.RED + "5 Kills:",
            ChatColor.GRAY + "• Blood Hook (Shift + Right-click)",
            "",
            ChatColor.DARK_PURPLE + "Kill Count: " + kills
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
