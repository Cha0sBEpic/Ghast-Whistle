package org.ghastWhistle.Listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityMountEvent;
import org.ghastWhistle.FollowManager;

public class MountListener implements Listener {
    private final FollowManager followManager;

    public MountListener(FollowManager followManager) {
        this.followManager = followManager;
    }

    @EventHandler
    public void onMount(EntityMountEvent event) {
        Entity var3 = event.getEntity();
        if (var3 instanceof Player player) {
            Entity var4 = event.getMount();
            if (var4 instanceof LivingEntity ghast) {
                this.followManager.cancelMountTimeout(ghast);
            }
        }
    }
}


