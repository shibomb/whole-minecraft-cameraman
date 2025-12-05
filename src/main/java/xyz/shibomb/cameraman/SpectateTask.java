package xyz.shibomb.cameraman;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import xyz.shibomb.cameraman.shots.CameraShot;

public class SpectateTask extends BukkitRunnable {

    private final Player cameraman;
    private final Entity target;
    private final CameraShot cameraShot;
    private long tick = 0;

    public SpectateTask(Player cameraman, Entity target, CameraShot cameraShot) {
        this.cameraman = cameraman;
        this.target = target;
        this.cameraShot = cameraShot;
    }

    @Override
    public void run() {
        if (cameraman == null || !cameraman.isOnline() || target == null || !target.isValid()) {
            this.cancel();
            return;
        }

        Location camLoc = cameraShot.getNextLocation(cameraman, target, tick);
        cameraman.teleport(camLoc);
        tick++;
    }

    // Static helper removed in favor of CameraShot interface
}
