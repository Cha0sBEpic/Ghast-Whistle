package org.ghastWhistle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class FollowManager {
    private final GhastWhistlePlugin plugin;
    private final Map<UUID, Integer> whistleFollow = new HashMap();
    private final Set<UUID> handFollow = new HashSet();
    private final Map<UUID, BukkitTask> mountTimeouts = new HashMap();
    private final NamespacedKey ownerKey;

    public FollowManager(GhastWhistlePlugin plugin) {
        this.plugin = plugin;
        this.ownerKey = new NamespacedKey(plugin, "ownerUUID");
        this.startFollowTask();
    }

    public void cancelMountTimeout(LivingEntity ghast) {
        BukkitTask task = (BukkitTask)this.mountTimeouts.remove(ghast.getUniqueId());
        if (task != null) {
            task.cancel();
        }

    }

    public void startMountTimeout(Player owner, LivingEntity ghast) {
        this.cancelMountTimeout(ghast);
        BukkitTask task = Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            if (this.shouldFollow(ghast.getUniqueId())) {
                this.handFollow.remove(ghast.getUniqueId());
                this.whistleFollow.remove(ghast.getUniqueId());
                ghast.setAI(true);
                owner.sendMessage("§cYour ghast lost interest and resumed flying.");
            }

        }, 100L);
        this.mountTimeouts.put(ghast.getUniqueId(), task);
    }

    public void startWhistleFollow(LivingEntity ghast) {
        this.whistleFollow.put(ghast.getUniqueId(), 300);
    }

    public void setHandFollow(LivingEntity ghast, boolean enable) {
        if (enable) {
            this.handFollow.add(ghast.getUniqueId());
        } else {
            this.handFollow.remove(ghast.getUniqueId());
            this.cancelMountTimeout(ghast);
        }

    }

    private boolean shouldFollow(UUID ghastId) {
        return this.whistleFollow.containsKey(ghastId) || this.handFollow.contains(ghastId);
    }

    private void startFollowTask() {
        (new BukkitRunnable() {
            public void run() {
                Iterator<Map.Entry<UUID, Integer>> it = FollowManager.this.whistleFollow.entrySet().iterator();

                while(it.hasNext()) {
                    Map.Entry<UUID, Integer> entry = (Map.Entry)it.next();
                    int ticks = (Integer)entry.getValue() - 1;
                    if (ticks <= 0) {
                        it.remove();
                    } else {
                        FollowManager.this.whistleFollow.put((UUID)entry.getKey(), ticks);
                    }
                }

                Set<UUID> toMove = new HashSet();
                toMove.addAll(FollowManager.this.whistleFollow.keySet());
                toMove.addAll(FollowManager.this.handFollow);

                for(UUID id : toMove) {
                    this.moveGhast(id);
                }

            }

            private void moveGhast(UUID id) {
                if (FollowManager.this.shouldFollow(id)) {
                    Entity e = Bukkit.getEntity(id);
                    if (e instanceof LivingEntity) {
                        LivingEntity ghast = (LivingEntity)e;
                        ItemStack harness = ghast.getEquipment().getItem(EquipmentSlot.BODY);
                        if (harness != null && harness.hasItemMeta()) {
                            PersistentDataContainer pdc = harness.getItemMeta().getPersistentDataContainer();
                            if (pdc.has(FollowManager.this.ownerKey, PersistentDataType.STRING)) {
                                UUID ownerId = UUID.fromString((String)pdc.get(FollowManager.this.ownerKey, PersistentDataType.STRING));
                                Player owner = Bukkit.getPlayer(ownerId);
                                if (owner != null) {
                                    Location pLoc = owner.getLocation();
                                    if (ghast.getPassengers().contains(owner)) {
                                        return;
                                    }
                                    Location gLoc = ghast.getLocation();
                                    double distance = gLoc.distance(pLoc);
                                    if (distance < (double)5.0F) {
                                        Vector playerVel = owner.getVelocity();
                                        boolean playerMovingTowardGhast = playerVel.lengthSquared() > 0.01 && playerVel.normalize().dot(gLoc.toVector().subtract(pLoc.toVector()).normalize()) > 0.4;
                                        if (!playerMovingTowardGhast) {
                                            ghast.setVelocity(new Vector(0, 0, 0));
                                            if (distance < (double)5.0F && !FollowManager.this.mountTimeouts.containsKey(ghast.getUniqueId())) {
                                                FollowManager.this.startMountTimeout(owner, ghast);
                                            }

                                            return;
                                        }
                                    }

                                    Vector dir = pLoc.toVector().subtract(gLoc.toVector());
                                    if (dir.lengthSquared() != (double)0.0F) {
                                        Vector velocity = dir.normalize().multiply(0.385);
                                        if (Double.isFinite(velocity.getX()) && Double.isFinite(velocity.getY()) && Double.isFinite(velocity.getZ())) {
                                            ghast.setVelocity(velocity);
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }).runTaskTimer(this.plugin, 1L, 1L);
    }
}

