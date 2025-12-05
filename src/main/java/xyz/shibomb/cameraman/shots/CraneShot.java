package xyz.shibomb.cameraman.shots;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import xyz.shibomb.cameraman.targets.CameraTarget;

public class CraneShot implements CameraShot {

    private final double durationTicks;
    private final double minHeight;
    private final double maxHeight;
    private final double distance;

    public CraneShot(double durationSeconds, double minHeight, double maxHeight, double distance) {
        this.durationTicks = durationSeconds * 20;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
        this.distance = distance;
    }

    @Override
    public Location getNextLocation(Player cameraman, CameraTarget target, long tick) {
        long cycleTick = (long) (tick % (durationTicks * 2));
        double t;

        if (cycleTick < durationTicks) {
            // Up: 0 -> 1
            t = (double) cycleTick / durationTicks;
        } else {
            // Down: 1 -> 0
            t = 1.0 - ((double) (cycleTick - durationTicks) / durationTicks);
        }

        // Easing
        t = t * t * (3 - 2 * t);

        double currentHeight = minHeight + ((maxHeight - minHeight) * t);

        Location targetLoc = target.getLocation();
        Vector direction = targetLoc.getDirection().setY(0).normalize();

        // Position: Behind + Height
        Location camLoc = targetLoc.clone().subtract(direction.multiply(distance));
        camLoc.add(0, currentHeight, 0);

        // Look at target
        // We want to look at the target's eyes/center, roughly
        camLoc.setDirection(targetLoc.clone().add(0, 1.0, 0).subtract(camLoc).toVector());

        return camLoc;
    }
}
