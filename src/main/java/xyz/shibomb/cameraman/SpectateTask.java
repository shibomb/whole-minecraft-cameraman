package xyz.shibomb.cameraman;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SpectateTask extends BukkitRunnable {

    private final Player cameraman;
    private final Entity target;
    private final SpectatePerspective perspective;
    private final double distance;
    private final double height;

    public SpectateTask(Player cameraman, Entity target, SpectatePerspective perspective, double distance,
            double height) {
        this.cameraman = cameraman;
        this.target = target;
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

        Location camLoc = calculateViewLocation(target, perspective, distance, height);
        cameraman.teleport(camLoc);
    }

    public static Location calculateViewLocation(Entity target, SpectatePerspective perspective, double distance,
            double height) {
        Location targetLoc = target.getLocation();
        Location camLoc = targetLoc.clone();

        if (perspective == SpectatePerspective.BEHIND) {
            // Position behind the target
            Vector direction = targetLoc.getDirection().normalize().multiply(-distance);
            camLoc.add(direction);
            camLoc.add(0, height, 0);
            camLoc.setDirection(targetLoc.toVector().subtract(camLoc.toVector()));
        } else if (perspective == SpectatePerspective.FRONT) {
            // Position in front of the target
            Vector direction = targetLoc.getDirection().normalize().multiply(distance);
            camLoc.add(direction);
            camLoc.add(0, height, 0);

            // Face the target
            Location lookAt = targetLoc.clone().add(0, 1.5, 0); // Look at head
            camLoc.setDirection(lookAt.toVector().subtract(camLoc.toVector()));
        }

        return camLoc;
    }
}
