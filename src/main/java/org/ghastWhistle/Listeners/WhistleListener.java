package org.ghastWhistle.Listeners;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.ghastWhistle.FollowManager;
import org.ghastWhistle.GhastWhistlePlugin;

public class WhistleListener implements Listener {
    private final GhastWhistlePlugin plugin;
    private final FollowManager followManager;

    public WhistleListener(GhastWhistlePlugin plugin, FollowManager followManager) {
        this.plugin = plugin;
        this.followManager = followManager;
    }

    @EventHandler
    public void onWhistle(PlayerInteractEvent event) {
        ItemStack horn = event.getItem();
        if (horn != null) {
            if (event.getAction().isRightClick()) {
                if ("Ghast Whistle".equalsIgnoreCase(horn.getItemMeta().getDisplayName())) {
                    event.setCancelled(true);
                    NamespacedKey ownerKey = new NamespacedKey(this.plugin, "ownerUUID");
                    String uuid = event.getPlayer().getUniqueId().toString();
                    int count = 0;

                    for(Entity e : event.getPlayer().getWorld().getNearbyEntities(event.getPlayer().getLocation(), (double)80.0F, (double)80.0F, (double)80.0F)) {
                        if (e.getType() == EntityType.valueOf("HAPPY_GHAST")) {
                            LivingEntity ghast = (LivingEntity)e;
                            ItemStack harness = ghast.getEquipment().getItem(EquipmentSlot.BODY);
                            if (harness != null && harness.hasItemMeta()) {
                                PersistentDataContainer pdc = harness.getItemMeta().getPersistentDataContainer();
                                if (pdc.has(ownerKey, PersistentDataType.STRING) && ((String)pdc.get(ownerKey, PersistentDataType.STRING)).equals(uuid)) {
                                    this.followManager.startWhistleFollow(ghast);
                                    Vector dir = event.getPlayer().getLocation().toVector().subtract(ghast.getLocation().toVector()).normalize();
                                    ghast.setVelocity(dir.multiply(0.40249999999999997));
                                    ++count;
                                }
                            }
                        }
                    }

                    if (count > 0) {
                        event.getPlayer().sendMessage("§aYour Happy Ghasts heard the whistle!");
                    } else {
                        event.getPlayer().sendMessage("§cNo Happy Ghasts linked to you heard the whistle.");
                    }

                }
            }
        }
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        ItemStack newItem = event.getPlayer().getInventory().getItem(event.getNewSlot());
        boolean holdingHorn = newItem != null && newItem.hasItemMeta() && "Ghast Whistle".equalsIgnoreCase(newItem.getItemMeta().getDisplayName());
        NamespacedKey ownerKey = new NamespacedKey(this.plugin, "ownerUUID");
        String uuid = event.getPlayer().getUniqueId().toString();

        for(Entity e : event.getPlayer().getWorld().getNearbyEntities(event.getPlayer().getLocation(), (double)80.0F, (double)80.0F, (double)80.0F)) {
            if (e.getType() == EntityType.valueOf("HAPPY_GHAST")) {
                LivingEntity ghast = (LivingEntity)e;
                ItemStack harness = ghast.getEquipment().getItem(EquipmentSlot.BODY);
                if (harness != null && harness.hasItemMeta()) {
                    PersistentDataContainer pdc = harness.getItemMeta().getPersistentDataContainer();
                    if (pdc.has(ownerKey, PersistentDataType.STRING) && ((String)pdc.get(ownerKey, PersistentDataType.STRING)).equals(uuid)) {
                        this.followManager.setHandFollow(ghast, holdingHorn);
                    }
                }
            }
        }

    }
}

