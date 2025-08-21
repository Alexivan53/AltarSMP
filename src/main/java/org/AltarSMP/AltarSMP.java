package org.AltarSMP;

import org.bukkit.plugin.java.JavaPlugin;
import org.AltarSMP.commands.GiveBoneBladeCommand;
import org.AltarSMP.commands.GiveCraftingMaterialsCommand;
import org.AltarSMP.commands.GiveBloodlustCommand;
import org.AltarSMP.commands.GiveWandOfIllusionCommand;
import org.AltarSMP.listeners.AbilityListener;
import org.AltarSMP.listeners.WardenDropListener;
import org.AltarSMP.recipes.CraftingRecipes;
// Removed custom GlowEnchantment; using vanilla UNBREAKING to provide glow

public final class AltarSMP extends JavaPlugin {

    @Override
    public void onEnable() {
        try {
            // No-op: custom Glow enchant removed in favor of vanilla enchant glow
            
            // Register commands with error checking
            if (getCommand("giveboneblade") != null) {
                getCommand("giveboneblade").setExecutor(new GiveBoneBladeCommand());
                getLogger().info("Registered giveboneblade command");
            } else {
                getLogger().severe("Could not find giveboneblade command in plugin.yml!");
            }
            
            if (getCommand("givecraftingmaterials") != null) {
                getCommand("givecraftingmaterials").setExecutor(new GiveCraftingMaterialsCommand(this));
                getLogger().info("Registered givecraftingmaterials command");
            } else {
                getLogger().severe("Could not find givecraftingmaterials command in plugin.yml!");
            }
            
            if (getCommand("givebloodlust") != null) {
                getCommand("givebloodlust").setExecutor(new GiveBloodlustCommand());
                getLogger().info("Registered givebloodlust command");
            } else {
                getLogger().severe("Could not find givebloodlust command in plugin.yml!");
            }
            
            if (getCommand("givewandofillusion") != null) {
                getCommand("givewandofillusion").setExecutor(new GiveWandOfIllusionCommand());
                getLogger().info("Registered givewandofillusion command");
            } else {
                getLogger().severe("Could not find givewandofillusion command in plugin.yml!");
            }
            
            // Register listeners
            getServer().getPluginManager().registerEvents(new AbilityListener(this), this);
            getServer().getPluginManager().registerEvents(new WardenDropListener(), this);
            
            // Register recipes
            CraftingRecipes.registerRecipes(this);
            
            getLogger().info("AltarSMP plugin has been enabled successfully!");
            
        } catch (Exception e) {
            getLogger().severe("Error enabling AltarSMP plugin: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("AltarSMP plugin has been disabled!");
    }
}
