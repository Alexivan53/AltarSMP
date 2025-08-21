package org.AltarSMP.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Warden;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.AltarSMP.items.CustomItems;

import java.util.Random;

public class WardenDropListener implements Listener {

    private final Random random = new Random();

    @EventHandler
    public void onWardenDeath(EntityDeathEvent event) {
        if (event.getEntityType() == EntityType.WARDEN) {
            if (random.nextDouble() < 0.20) {
                event.getDrops().add(CustomItems.createWardensHeart());
            }
        }
    }
}
