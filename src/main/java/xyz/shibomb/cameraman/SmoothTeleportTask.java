package xyz.shibomb.cameraman;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SmoothTeleportTask extends BukkitRunnable {

    private final Player cameraman;
    private final Entity target;
    private final Location startLocation;
    private final long durationTicks;
    private final Runnable onComplete;
    private final SpectatePerspective perspective;
    private final double distance;
    private final double height;
    private long currentTick = 0;

    public SmoothTeleportTask(Player cameraman, Entity target, long durationTicks, Runnable onComplete,
            SpectatePerspective perspective, double distance, double height) {
        this.cameraman = cameraman;
        this.target = target;
        this.startLocation = cameraman.getLocation();
        this.durationTicks = durationTicks;
        this.onComplete = onComplete;
        this.perspective = perspective;
        this.distance = distance;
        this.height = height;
    }

    @Override
    public void run() {
        if (cameraman == null || !cameraman.isOnline() || target == null || !target.isValid()) {
            this.cancel();
            return;
        }

        if (currentTick >= durationTicks) {
            this.cancel();
            onComplete.run();
            return;
        }

        double t = (double) currentTick / durationTicks;
        // Smooth step interpolation for nicer easing
        t = t * t * (3 - 2 * t);

        Location targetLoc = SpectateTask.calculateViewLocation(target, perspective, distance, height);
        Vector startVec = startLocation.toVector();
        Vector targetVec = targetLoc.toVector();

        // Interpolate position
        Vector currentVec = startVec.clone().add(targetVec.clone().subtract(startVec).multiply(t));

        Location currentLoc = currentVec.toLocation(startLocation.getWorld());

        // Interpolate rotation (yaw/pitch)
        float startYaw = startLocation.getYaw();
        float targetYaw = targetLoc.getYaw();

        // Handle yaw wrapping (e.g. 350 -> 10 should be +20, not -340)
        float diffYaw = targetYaw - startYaw;
        while (diffYaw < -180)
            diffYaw += 360;
        while (diffYaw >= 180)
            diffYaw -= 360;

        float currentYaw = startYaw + (diffYaw * (float) t);
        float currentPitch = startLocation.getPitch() + ((targetLoc.getPitch() - startLocation.getPitch()) * (float) t);

        currentLoc.setYaw(currentYaw);
        currentLoc.setPitch(currentPitch);

        cameraman.teleport(currentLoc);

        currentTick++;
    }
}
