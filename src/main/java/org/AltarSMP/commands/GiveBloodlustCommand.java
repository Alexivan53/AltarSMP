package org.AltarSMP.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.AltarSMP.items.CustomItems;

public class GiveBloodlustCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        
        if (!player.hasPermission("altarsmp.givebloodlust")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        // Give the Bloodlust sword
        player.getInventory().addItem(CustomItems.createBloodlust());
        
        player.sendMessage(ChatColor.DARK_RED + "Bloodlust: " + ChatColor.GREEN + "You have received the Bloodlust sword!");
        player.sendMessage(ChatColor.GRAY + "Kill players to unlock progressive abilities!");
        
        return true;
    }
}
