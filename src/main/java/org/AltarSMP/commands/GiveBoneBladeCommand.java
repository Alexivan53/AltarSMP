package org.AltarSMP.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.AltarSMP.items.CustomItems;

public class GiveBoneBladeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        
        player.getInventory().addItem(CustomItems.createBoneBlade());
        player.sendMessage(ChatColor.GREEN + "You have received the Bone Blade!");
        
        return true;
    }
}
