package org.AltarSMP.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.AltarSMP.items.CustomItems;

public class GiveWandOfIllusionCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        
        if (!player.hasPermission("altarsmp.givewandofillusion")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        // Give the Wand of Illusion
        player.getInventory().addItem(CustomItems.createWandOfIllusion());
        
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Wand of Illusion: " + ChatColor.GREEN + "You have received the Wand of Illusion!");
        player.sendMessage(ChatColor.GRAY + "Right-click to transform into the last mob you killed!");
        
        return true;
    }
}
