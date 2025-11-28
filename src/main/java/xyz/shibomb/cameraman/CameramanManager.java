package xyz.shibomb.cameraman;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CameramanManager {

    private final JavaPlugin plugin;
    private UUID cameramanId;
    private boolean newcomerMode = false;
    private boolean rotationMode = false;
    private RotationTask rotationTask;
    private long rotationInterval = 200L; // Default 10 seconds (20 ticks * 10)
    private boolean mobTargetMode = false;
    private boolean autoMobTarget = false;
    private long autoMobTargetDelay = 5L; // Seconds
    private long lastPlayerTargetTime = System.currentTimeMillis();
    private boolean teleportSmooth = false;
    private long teleportSmoothDuration = 3L; // Seconds
    private SmoothTeleportTask currentTeleportTask;

    public CameramanManager(JavaPlugin plugin) {
        this.plugin = plugin;
        String uuidString = plugin.getConfig().getString("cameraman");
        if (uuidString != null) {
            try {
                this.cameramanId = UUID.fromString(uuidString);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid cameraman UUID in config: " + uuidString);
            }
        }

        this.newcomerMode = plugin.getConfig().getBoolean("newcomerMode", false);
        this.rotationMode = plugin.getConfig().getBoolean("rotationMode", false);
        long intervalSeconds = plugin.getConfig().getLong("rotationInterval", 10L);
        this.rotationInterval = intervalSeconds * 20L;

        this.mobTargetMode = plugin.getConfig().getBoolean("mobTargetMode", false);
        this.autoMobTarget = plugin.getConfig().getBoolean("autoMobTarget", false);
        this.autoMobTargetDelay = plugin.getConfig().getLong("autoMobTargetDelay", 5L);

        this.teleportSmooth = plugin.getConfig().getBoolean("teleportSmooth", false);
        this.teleportSmoothDuration = plugin.getConfig().getLong("teleportSmoothDuration", 3L);

        checkAndStartRotationTask();
    }

    private void checkAndStartRotationTask() {
        boolean shouldRun = rotationMode || mobTargetMode || autoMobTarget;

        // Always stop the existing task to ensure we use the latest interval/settings
        stopRotationTask();

        if (shouldRun) {
            this.rotationTask = new RotationTask(this);
            this.rotationTask.runTaskTimer(plugin, 0L, this.rotationInterval);
        }
    }

    public void setCameraman(Player player) {
        if (this.cameramanId != null && !this.cameramanId.equals(player.getUniqueId())) {
            Player oldCameraman = Bukkit.getPlayer(this.cameramanId);
            if (oldCameraman != null) {
                oldCameraman.setGameMode(GameMode.SURVIVAL);
                oldCameraman.sendMessage("You are no longer the cameraman.");
            }
        }
        this.cameramanId = player.getUniqueId();
        plugin.getConfig().set("cameraman", this.cameramanId.toString());
        plugin.saveConfig();
        player.setGameMode(GameMode.SPECTATOR);
        player.sendMessage("You are now the cameraman!");
    }

    public Player getCameraman() {
        if (cameramanId == null)
            return null;
        return Bukkit.getPlayer(cameramanId);
    }

    public void restoreCameraman(Player player) {
        if (cameramanId != null && cameramanId.equals(player.getUniqueId())) {
            player.setGameMode(GameMode.SPECTATOR);
            player.sendMessage("Welcome back, Cameraman!");
            player.sendMessage("Newcomer Mode: " + newcomerMode);
            player.sendMessage("Rotation Mode: " + rotationMode
                    + (rotationMode ? " (Interval: " + (rotationInterval / 20) + "s)" : ""));
            player.sendMessage("Mob Target Mode: " + mobTargetMode);
            player.sendMessage("Auto Mob Target: " + autoMobTarget
                    + (autoMobTarget ? " (Delay: " + autoMobTargetDelay + "s)" : ""));
            player.sendMessage("Smooth Teleport: " + teleportSmooth
                    + (teleportSmooth ? " (Duration: " + teleportSmoothDuration + "s)" : ""));
        }
    }

    public void setTarget(org.bukkit.entity.Entity target) {
        Player cameraman = getCameraman();
        if (cameraman != null && target != null) {
            if (target instanceof Player) {
                if (cameraman.getUniqueId().equals(target.getUniqueId())) {
                    return; // Cannot spectate self
                }
                // If targeting a player, disable mob target mode (conceptually, we are focusing
                // on a player now)
                if (mobTargetMode) {
                    setMobTargetMode(false);
                    cameraman.sendMessage("Mob Target Mode disabled because a player was targeted.");
                }
                lastPlayerTargetTime = System.currentTimeMillis();
            }

            if (cameraman.getGameMode() != GameMode.SPECTATOR) {
                cameraman
                        .sendMessage("Cannot spectate " + target.getName() + " because you are not in Spectator mode.");
                return;
            }

            // Stop any existing smooth teleport task
            if (currentTeleportTask != null && !currentTeleportTask.isCancelled()) {
                currentTeleportTask.cancel();
            }

            cameraman.setSpectatorTarget(null); // Reset first

            if (teleportSmooth) {
                cameraman.sendMessage("Moving to " + target.getName() + "...");
                currentTeleportTask = new SmoothTeleportTask(cameraman, target, teleportSmoothDuration * 20L, () -> {
                    cameraman.setSpectatorTarget(target);
                    cameraman.sendMessage("Now spectating: " + target.getName());
                });
                currentTeleportTask.runTaskTimer(plugin, 0L, 1L);
            } else {
                cameraman.setSpectatorTarget(target);
                cameraman.sendMessage("Now spectating: " + target.getName());
            }
        }
    }

    public void clearTarget() {
        Player cameraman = getCameraman();
        if (cameraman != null) {
            cameraman.setSpectatorTarget(null);
            cameraman.sendMessage("Stopped spectating.");
        }
    }

    public void setNewcomerMode(boolean enabled) {
        this.newcomerMode = enabled;
        plugin.getConfig().set("newcomerMode", enabled);
        plugin.saveConfig();
        Player cameraman = getCameraman();
        if (cameraman != null) {
            cameraman.sendMessage("Newcomer mode set to: " + enabled);
        }
    }

    public boolean isNewcomerMode() {
        return newcomerMode;
    }

    public void setRotationMode(boolean enabled, long intervalSeconds) {
        this.rotationMode = enabled;

        plugin.getConfig().set("rotationMode", enabled);
        if (enabled) {
            plugin.getConfig().set("rotationInterval", intervalSeconds);
            this.rotationInterval = intervalSeconds * 20L;
        }
        plugin.saveConfig();

        checkAndStartRotationTask();

        Player cameraman = getCameraman();
        if (cameraman != null) {
            cameraman.sendMessage("Rotation mode set to: " + enabled + " (Interval: " + intervalSeconds + "s)");
        }
    }

    public void stopRotationTask() {
        if (this.rotationTask != null && !this.rotationTask.isCancelled()) {
            this.rotationTask.cancel();
        }
    }

    public List<Player> getPotentialTargets() {
        List<Player> targets = new ArrayList<>(Bukkit.getOnlinePlayers());
        // Remove cameraman from targets
        if (cameramanId != null) {
            targets.removeIf(p -> p.getUniqueId().equals(cameramanId));
        }
        // Remove spectators
        targets.removeIf(p -> p.getGameMode() == GameMode.SPECTATOR);
        return targets;
    }

    public void setMobTargetMode(boolean enabled) {
        this.mobTargetMode = enabled;
        plugin.getConfig().set("mobTargetMode", enabled);
        plugin.saveConfig();

        checkAndStartRotationTask();

        Player cameraman = getCameraman();
        if (cameraman != null) {
            cameraman.sendMessage("Mob Target Mode set to: " + enabled);
        }
    }

    public boolean isMobTargetMode() {
        return mobTargetMode;
    }

    public void setAutoMobTarget(boolean enabled, long delay) {
        this.autoMobTarget = enabled;
        this.autoMobTargetDelay = delay;
        plugin.getConfig().set("autoMobTarget", enabled);
        plugin.getConfig().set("autoMobTargetDelay", delay);
        plugin.saveConfig();

        checkAndStartRotationTask();

        Player cameraman = getCameraman();
        if (cameraman != null) {
            cameraman.sendMessage("Auto Mob Target set to: " + enabled + " (Delay: " + delay + "s)");
        }
    }

    public boolean isAutoMobTarget() {
        return autoMobTarget;
    }

    public long getAutoMobTargetDelay() {
        return autoMobTargetDelay;
    }

    public long getLastPlayerTargetTime() {
        return lastPlayerTargetTime;
    }

    public List<org.bukkit.entity.LivingEntity> getPotentialMobTargets() {
        Player cameraman = getCameraman();
        List<org.bukkit.entity.LivingEntity> mobs = new ArrayList<>();
        if (cameraman != null) {
            for (org.bukkit.entity.Entity entity : cameraman.getNearbyEntities(50, 50, 50)) {
                if (entity instanceof org.bukkit.entity.LivingEntity && !(entity instanceof Player)
                        && !(entity instanceof org.bukkit.entity.ArmorStand)) {
                    mobs.add((org.bukkit.entity.LivingEntity) entity);
                }
            }
        }
        return mobs;
    }

    public void setTeleportSmooth(boolean enabled, long duration) {
        this.teleportSmooth = enabled;
        this.teleportSmoothDuration = duration;
        plugin.getConfig().set("teleportSmooth", enabled);
        plugin.getConfig().set("teleportSmoothDuration", duration);
        plugin.saveConfig();

        Player cameraman = getCameraman();
        if (cameraman != null) {
            cameraman.sendMessage("Smooth Teleport set to: " + enabled + " (Duration: " + duration + "s)");
        }
    }
}
