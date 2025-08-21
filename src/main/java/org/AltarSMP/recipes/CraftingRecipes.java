package org.AltarSMP.recipes;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.AltarSMP.AltarSMP;
import org.AltarSMP.items.CustomItems;

public class CraftingRecipes {

    public static void registerRecipes(AltarSMP plugin) {
        registerWeaponHandleRecipe(plugin);
        
        registerBoneBladeRecipe(plugin);
    }

    private static void registerWeaponHandleRecipe(AltarSMP plugin) {
        NamespacedKey handleKey = new NamespacedKey(plugin, "weapon_handle");
        ShapedRecipe handleRecipe = new ShapedRecipe(handleKey, CustomItems.createWeaponHandle());
        
        handleRecipe.shape(" I ", " B ", " I ");
        handleRecipe.setIngredient('I', Material.IRON_INGOT);
        handleRecipe.setIngredient('B', Material.BREEZE_ROD);
        
        plugin.getServer().addRecipe(handleRecipe);
    }

    private static void registerBoneBladeRecipe(AltarSMP plugin) {
        NamespacedKey bladeKey = new NamespacedKey(plugin, "bone_blade");
        ShapedRecipe bladeRecipe = new ShapedRecipe(bladeKey, CustomItems.createBoneBlade());
        
        bladeRecipe.shape("BSW", "BIB", "BHB");
        bladeRecipe.setIngredient('B', Material.BONE_BLOCK);
        bladeRecipe.setIngredient('S', Material.SKELETON_SKULL);
        bladeRecipe.setIngredient('W', Material.WITHER_SKELETON_SKULL);
        bladeRecipe.setIngredient('I', Material.IRON_BLOCK);
        bladeRecipe.setIngredient('H', new RecipeChoice.ExactChoice(CustomItems.createWeaponHandle()));
        
        plugin.getServer().addRecipe(bladeRecipe);
    }
}
