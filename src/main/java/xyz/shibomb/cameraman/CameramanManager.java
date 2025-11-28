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
    private boolean spectateMode = true;
    private boolean mobNightVision = false;
    private boolean showMessage = true;
    private org.bukkit.potion.PotionEffect previousNightVisionEffect = null;
    private long previousNightVisionTime = 0;

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

        this.spectateMode = plugin.getConfig().getBoolean("spectateMode", true);
        this.mobNightVision = plugin.getConfig().getBoolean("mobNightVision", false);
        this.showMessage = plugin.getConfig().getBoolean("showMessage", true);

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
            player.sendMessage("Spectate Mode: " + spectateMode);
            player.sendMessage("Mob Night Vision: " + mobNightVision);
            player.sendMessage("Show Message: " + showMessage);
        }
    }

    private void sendInfoMessage(Player player, String message) {
        if (showMessage && player != null) {
            player.sendMessage(message);
        }
    }

    public void setTarget(org.bukkit.entity.Entity target) {
        Player cameraman = getCameraman();
        if (cameraman != null && target != null) {
            // Clean up previous night vision states first
            removeCameramanNightVision(cameraman);

            if (target instanceof Player) {
                if (cameraman.getUniqueId().equals(target.getUniqueId())) {
                    return; // Cannot spectate self
                }
                // If targeting a player, disable mob target mode (conceptually, we are focusing
                // on a player now)
                if (mobTargetMode) {
                    setMobTargetMode(false);
                    sendInfoMessage(cameraman, "Mob Target Mode disabled because a player was targeted.");
                }
                lastPlayerTargetTime = System.currentTimeMillis();
            }

            if (cameraman.getGameMode() != GameMode.SPECTATOR) {
                sendInfoMessage(cameraman,
                        "Cannot spectate " + target.getName() + " because you are not in Spectator mode.");
                return;
            }

            // Stop any existing smooth teleport task
            if (currentTeleportTask != null && !currentTeleportTask.isCancelled()) {
                currentTeleportTask.cancel();
            }

            cameraman.setSpectatorTarget(null); // Reset first

            Runnable onTargetSet = () -> {
                if (spectateMode) {
                    cameraman.setSpectatorTarget(target);
                    sendInfoMessage(cameraman, "Now spectating: " + target.getName());
                } else {
                    sendInfoMessage(cameraman, "Arrived at: " + target.getName());
                }

                // Apply Night Vision to CAMERAMAN if applicable
                if (mobNightVision && target instanceof org.bukkit.entity.LivingEntity && !(target instanceof Player)) {
                    applyCameramanNightVision(cameraman);
                }
            };

            if (teleportSmooth) {
                sendInfoMessage(cameraman, "Moving to " + target.getName() + "...");
                currentTeleportTask = new SmoothTeleportTask(cameraman, target, teleportSmoothDuration * 20L,
                        onTargetSet);
                currentTeleportTask.runTaskTimer(plugin, 0L, 1L);
            } else {
                if (spectateMode) {
                    onTargetSet.run();
                } else {
                    cameraman.teleport(target);
                    sendInfoMessage(cameraman, "Moved to: " + target.getName());

                    // Apply Night Vision to CAMERAMAN if applicable (Instant teleport case)
                    if (mobNightVision && target instanceof org.bukkit.entity.LivingEntity
                            && !(target instanceof Player)) {
                        applyCameramanNightVision(cameraman);
                    }
                }
            }
        }
    }

    public void clearTarget() {
        Player cameraman = getCameraman();
        if (cameraman != null) {
            cameraman.setSpectatorTarget(null);
            removeCameramanNightVision(cameraman);
            sendInfoMessage(cameraman, "Stopped spectating.");
        }
    }

    private void applyCameramanNightVision(Player player) {
        org.bukkit.potion.PotionEffect current = player
                .getPotionEffect(org.bukkit.potion.PotionEffectType.NIGHT_VISION);
        // If current is null or not our infinite one (duration < 1000000 is a safe bet
        // for "not infinite")
        // We assume our infinite one is Integer.MAX_VALUE
        if (current != null && current.getDuration() < 1000000) {
            previousNightVisionEffect = current;
            previousNightVisionTime = System.currentTimeMillis();
        }

        // If we don't have a current effect, or we just saved it, apply ours
        if (current == null || current.getDuration() < 1000000) {
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.NIGHT_VISION,
                    Integer.MAX_VALUE, 0, false, false));
        }
    }

    private void removeCameramanNightVision(Player player) {
        // First, check if we have the infinite effect. If so, remove it.
        org.bukkit.potion.PotionEffect current = player
                .getPotionEffect(org.bukkit.potion.PotionEffectType.NIGHT_VISION);
        if (current != null && current.getDuration() > 1000000) {
            player.removePotionEffect(org.bukkit.potion.PotionEffectType.NIGHT_VISION);
        }

        // Restore previous effect if it exists
        if (previousNightVisionEffect != null) {
            long elapsedTicks = (System.currentTimeMillis() - previousNightVisionTime) / 50;
            int remaining = previousNightVisionEffect.getDuration() - (int) elapsedTicks;

            if (remaining > 0) {
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        previousNightVisionEffect.getType(),
                        remaining,
                        previousNightVisionEffect.getAmplifier(),
                        previousNightVisionEffect.isAmbient(),
                        previousNightVisionEffect.hasParticles(),
                        previousNightVisionEffect.hasIcon()));
            }
            previousNightVisionEffect = null;
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

    public void setSpectateMode(boolean enabled) {
        this.spectateMode = enabled;
        plugin.getConfig().set("spectateMode", enabled);
        plugin.saveConfig();

        Player cameraman = getCameraman();
        if (cameraman != null) {
            cameraman.sendMessage("Spectate Mode set to: " + enabled);
        }
    }

    public void setMobNightVision(boolean enabled) {
        this.mobNightVision = enabled;
        plugin.getConfig().set("mobNightVision", enabled);
        plugin.saveConfig();

        Player cameraman = getCameraman();
        if (cameraman != null) {
            cameraman.sendMessage("Mob Night Vision set to: " + enabled);
        }
    }

    public void setShowMessage(boolean enabled) {
        this.showMessage = enabled;
        plugin.getConfig().set("showMessage", enabled);
        plugin.saveConfig();

        Player cameraman = getCameraman();
        if (cameraman != null) {
            cameraman.sendMessage("Show Message set to: " + enabled);
        }
    }
}
