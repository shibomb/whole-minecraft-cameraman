package com.shibomb.cameraman;

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
        List<Player> targets = manager.getPotentialTargets();
        if (targets.isEmpty()) {
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
