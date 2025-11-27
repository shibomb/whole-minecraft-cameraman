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
        if (cameramanId == null) return null;
        return Bukkit.getPlayer(cameramanId);
    }

    public void restoreCameraman(Player player) {
        if (cameramanId != null && cameramanId.equals(player.getUniqueId())) {
            player.setGameMode(GameMode.SPECTATOR);
            player.sendMessage("Welcome back, Cameraman!");
            player.sendMessage("Newcomer Mode: " + newcomerMode);
            player.sendMessage("Rotation Mode: " + rotationMode + (rotationMode ? " (Interval: " + (rotationInterval / 20) + "s)" : ""));
        }
    }

    public void setTarget(Player target) {
        Player cameraman = getCameraman();
        if (cameraman != null && target != null) {
            if (cameraman.getUniqueId().equals(target.getUniqueId())) {
                return; // Cannot spectate self
            }
            
            if (cameraman.getGameMode() != GameMode.SPECTATOR) {
                cameraman.sendMessage("Cannot spectate " + target.getName() + " because you are not in Spectator mode.");
                return;
            }

            cameraman.setSpectatorTarget(null); // Reset first
            cameraman.setSpectatorTarget(target);
            cameraman.sendMessage("Now spectating: " + target.getName());
        }
    }

    public void setNewcomerMode(boolean enabled) {
        this.newcomerMode = enabled;
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
        stopRotationTask();

        if (enabled) {
            this.rotationInterval = intervalSeconds * 20L;
            this.rotationTask = new RotationTask(this);
            this.rotationTask.runTaskTimer(plugin, 0L, this.rotationInterval);
        }
        
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
}
