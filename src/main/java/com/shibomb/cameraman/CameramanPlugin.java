package com.shibomb.cameraman;

import org.bukkit.plugin.java.JavaPlugin;

public final class CameramanPlugin extends JavaPlugin {

    private CameramanManager cameramanManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        getLogger().info("Cameraman plugin has been enabled!");

        this.cameramanManager = new CameramanManager(this);
        
        getCommand("cameraman").setExecutor(new CameramanCommand(this.cameramanManager));
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this.cameramanManager), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (this.cameramanManager != null) {
            this.cameramanManager.stopRotationTask();
        }
        getLogger().info("Cameraman plugin has been disabled!");
    }

    public CameramanManager getCameramanManager() {
        return cameramanManager;
    }
}
