package org.AltarSMP;

import org.bukkit.plugin.java.JavaPlugin;
import org.AltarSMP.commands.GiveBoneBladeCommand;
import org.AltarSMP.commands.GiveCraftingMaterialsCommand;
import org.AltarSMP.commands.GiveBloodlustCommand;
import org.AltarSMP.commands.GiveWandOfIllusionCommand;
import org.AltarSMP.listeners.AbilityListener;
import org.AltarSMP.commands.AltarCommand;
import org.AltarSMP.listeners.WardenDropListener;
import org.AltarSMP.recipes.CraftingRecipes;

public final class AltarSMP extends JavaPlugin {

    @Override
    public void onEnable() {
        try {

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

            if (getCommand("altar") != null) {
                AltarCommand altarCmd = new AltarCommand(this);
                getCommand("altar").setExecutor(altarCmd);
                getCommand("altar").setTabCompleter(altarCmd);
                getLogger().info("Registered altar command");
            } else {
                getLogger().severe("Could not find altar command in plugin.yml!");
            }

            getServer().getPluginManager().registerEvents(new AbilityListener(this), this);
            getServer().getPluginManager().registerEvents(new WardenDropListener(), this);
            

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
