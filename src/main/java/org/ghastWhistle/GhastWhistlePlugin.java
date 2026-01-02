package org.ghastWhistle;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.ghastWhistle.Listeners.HarnessListener;
import org.ghastWhistle.Listeners.MountListener;
import org.ghastWhistle.Listeners.WhistleListener;

public class GhastWhistlePlugin extends JavaPlugin {
    private static GhastWhistlePlugin instance;
    private FollowManager followManager;

    public void onEnable() {
        instance = this;
        this.followManager = new FollowManager(this);
        Bukkit.getPluginManager().registerEvents(new HarnessListener(this), this);
        Bukkit.getPluginManager().registerEvents(new WhistleListener(this, this.followManager), this);
        this.getServer().getPluginManager().registerEvents(new MountListener(this.followManager), this);
        this.getLogger().info("Ghast Whistle Plugin enabled.");
        Object var2 = null;
    }

    public FollowManager getFollowManager() {
        return this.followManager;
    }

    public static GhastWhistlePlugin getInstance() {
        return instance;
    }
}

