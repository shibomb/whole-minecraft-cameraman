package xyz.shibomb.cameraman;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import xyz.shibomb.cameraman.shots.CameraShot;

public class SmoothTeleportTask extends BukkitRunnable {

    private final Player cameraman;
    private final Entity target;
    private final long totalTicks;
    private final Runnable onComplete;
    private final CameraShot cameraShot;
    private long currentTick = 0;
    private Location startLoc;

    public SmoothTeleportTask(Player cameraman, Entity target, long durationTicks, Runnable onComplete,
            CameraShot cameraShot) {
        this.cameraman = cameraman;
        this.target = target;
        this.totalTicks = durationTicks;
        this.onComplete = onComplete;
        this.cameraShot = cameraShot;
        this.startLoc = cameraman.getLocation();
    }

    @Override
    public void run() {
        if (cameraman == null || !cameraman.isOnline() || target == null || !target.isValid()) {
            this.cancel();
            return;
        }

        if (currentTick >= totalTicks) {
            this.cancel();
            onComplete.run();
            return;
        }

        double t = (double) currentTick / totalTicks;
        // Smooth step interpolation for nicer easing
        t = t * t * (3 - 2 * t);

        // Calculate the target position for this frame (or final destination)
        // For smooth teleport to static shot: target is constant.
        // For smooth teleport to orbit: target is moving.
        // We'll aim for the position at tick 0 of the shot.
        Location targetLoc = cameraShot.getNextLocation(cameraman, target, 0);
        Vector startVec = startLoc.toVector();
        Vector targetVec = targetLoc.toVector();

        // Interpolate position
        Vector currentVec = startVec.clone().add(targetVec.clone().subtract(startVec).multiply(t));

        Location currentLoc = currentVec.toLocation(startLoc.getWorld());

        // Interpolate rotation (yaw/pitch)
        float startYaw = startLoc.getYaw();
        float targetYaw = targetLoc.getYaw();

        // Handle yaw wrapping (e.g. 350 -> 10 should be +20, not -340)
        float diffYaw = targetYaw - startYaw;
        while (diffYaw < -180)
            diffYaw += 360;
        while (diffYaw >= 180)
            diffYaw -= 360;

        float currentYaw = startYaw + (diffYaw * (float) t);
        float currentPitch = startLoc.getPitch() + ((targetLoc.getPitch() - startLoc.getPitch()) * (float) t);

        currentLoc.setYaw(currentYaw);
        currentLoc.setPitch(currentPitch);

        cameraman.teleport(currentLoc);

        currentTick++;
    }
}
