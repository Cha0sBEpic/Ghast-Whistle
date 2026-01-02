package org.ghastWhistle.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.ghastWhistle.GhastWhistlePlugin;

public class HarnessListener implements Listener {
    private final GhastWhistlePlugin plugin;

    public HarnessListener(GhastWhistlePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onHarnessAttach(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getType() == EntityType.valueOf("HAPPY_GHAST")) {
            LivingEntity ghast = (LivingEntity)event.getRightClicked();
            ItemStack hand = event.getPlayer().getInventory().getItem(event.getHand());
            if (hand != null) {
                if (hand.getType().name().contains("HARNESS")) {
                    Bukkit.getScheduler().runTask(this.plugin, () -> {
                        ItemStack harness = ghast.getEquipment().getItem(EquipmentSlot.BODY);
                        if (harness != null) {
                            ItemMeta meta = harness.getItemMeta();
                            PersistentDataContainer pdc = meta.getPersistentDataContainer();
                            NamespacedKey ownerUUID = new NamespacedKey(this.plugin, "ownerUUID");
                            NamespacedKey ownerName = new NamespacedKey(this.plugin, "ownerName");
                            if (!pdc.has(ownerUUID, PersistentDataType.STRING)) {
                                pdc.set(ownerUUID, PersistentDataType.STRING, event.getPlayer().getUniqueId().toString());
                                pdc.set(ownerName, PersistentDataType.STRING, event.getPlayer().getName());
                                harness.setItemMeta(meta);
                                ghast.getEquipment().setItem(EquipmentSlot.BODY, harness);
                                event.getPlayer().sendMessage("Â§aHarness linked to you.");
                            }

                        }
                    });
                }
            }
        }
    }
}
