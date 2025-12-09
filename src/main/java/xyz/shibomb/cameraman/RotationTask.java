package xyz.shibomb.cameraman;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class RotationTask extends BukkitRunnable {

    private final CameramanManager manager;
    private int currentIndex = 0;

    public RotationTask(CameramanManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        Player cameraman = manager.getCameraman();
        if (cameraman == null || !cameraman.isOnline())
            return;

        double minDistance = manager.getMinRotationDistance();

        if (manager.isMobTargetMode()) {
            List<org.bukkit.entity.LivingEntity> mobs = manager.getPotentialMobTargets();
            if (mobs.isEmpty()) {
                return;
            }

            // Find next valid target
            int attempts = 0;
            // Loop at most mobs.size() times to find a valid target
            while (attempts < mobs.size()) {
                if (currentIndex >= mobs.size()) {
                    currentIndex = 0;
                }

                org.bukkit.entity.LivingEntity candidate = mobs.get(currentIndex);
                currentIndex++; // Always advance index for next time
                attempts++;

                // If minDistance is set (> 0), check distance
                if (minDistance > 0.0) {
                    try {
                        // Check world first
                        if (cameraman.getWorld().equals(candidate.getWorld())) {
                            if (cameraman.getLocation().distance(candidate.getLocation()) < minDistance) {
                                // Too close, skip
                                continue;
                            }
                        }
                        // If different worlds, distance check is N/A (or effectively infinite), so
                        // valid.
                    } catch (IllegalArgumentException e) {
                        // Different worlds
                    }
                }

                // Found valid target
                manager.setTarget(candidate);
                return;
            }

            // If we looped and found nothing valid (all too close), fall back to next one
            int forceIndex = (currentIndex) % mobs.size();
            manager.setTarget(mobs.get(forceIndex));
            currentIndex++;

        } else {
            List<Player> targets = manager.getPotentialTargets();
            if (targets.isEmpty()) {
                if (manager.isAutoMobTarget()) {
                    long timeSinceLastPlayer = System.currentTimeMillis() - manager.getLastPlayerTargetTime();
                    if (timeSinceLastPlayer > manager.getAutoMobTargetDelay() * 1000) {
                        manager.setMobTargetMode(true);
                        if (manager.getCameraman() != null) {
                            manager.getCameraman().sendMessage("Auto-switched to Mob Target Mode due to inactivity.");
                        }
                    }
                }
                return;
            }

            // Find next valid target
            int attempts = 0;
            while (attempts < targets.size()) {
                if (currentIndex >= targets.size()) {
                    currentIndex = 0;
                }

                Player candidate = targets.get(currentIndex);
                currentIndex++;
                attempts++;

                if (minDistance > 0.0) {
                    try {
                        if (cameraman.getWorld().equals(candidate.getWorld())) {
                            if (cameraman.getLocation().distance(candidate.getLocation()) < minDistance) {
                                // Too close
                                continue;
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        // Different worlds
                    }
                }

                manager.setTarget(candidate);
                return;
            }

            // Fallback if all skipped
            int forceIndex = (currentIndex) % targets.size();
            manager.setTarget(targets.get(forceIndex));
            currentIndex++;
        }
    }
}
