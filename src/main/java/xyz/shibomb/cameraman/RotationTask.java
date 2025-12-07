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
        if (manager.isMobTargetMode()) {
            List<org.bukkit.entity.LivingEntity> mobs = manager.getPotentialMobTargets();
            if (mobs.isEmpty()) {
                return;
            }
            if (currentIndex >= mobs.size()) {
                currentIndex = 0;
            }
            manager.setTarget(mobs.get(currentIndex));
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

            if (currentIndex >= targets.size()) {
                currentIndex = 0;
            }

            Player target = targets.get(currentIndex);
            manager.setTarget(target);

            currentIndex++;
        }
    }
}
