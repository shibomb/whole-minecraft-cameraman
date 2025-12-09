package xyz.shibomb.cameraman.shots;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import xyz.shibomb.cameraman.SpectatePerspective;
import xyz.shibomb.cameraman.targets.CameraTarget;

public class StaticShot implements CameraShot {

    private final SpectatePerspective perspective;
    private final double distance;
    private final double height;

    public StaticShot(SpectatePerspective perspective, double distance, double height) {
        this.perspective = perspective;
        this.distance = distance;
        this.height = height;
    }

    @Override
    public Location getNextLocation(Player cameraman, CameraTarget target, long tick) {
        Location targetLoc = target.getLocation();
        Vector direction = targetLoc.getDirection();

        Location camLoc;

        if (perspective == SpectatePerspective.FIX) {
            // FIX: Just return target location + height.
            // For Scenic (LocationTarget), this keeps the original yaw/pitch.
            // For EntityTarget, it keeps the entity's current location/yaw/pitch (which
            // changes).
            // But since this is mainly for Scenic, static behavior is desired.
            camLoc = targetLoc.clone().add(0, height, 0);
            return camLoc;
        }

        if (perspective == SpectatePerspective.FRONT) {
            // In front: target + direction * distance
            camLoc = targetLoc.clone().add(direction.clone().multiply(distance));
            camLoc.add(0, height, 0);

            // Look AT target (inverse of direction)
            camLoc.setDirection(direction.clone().multiply(-1));
        } else {
            // BEHIND (Default)
            // Behind: target - direction * distance
            camLoc = targetLoc.clone().subtract(direction.clone().multiply(distance));
            camLoc.add(0, height, 0);

            // Look SAME direction as target
            camLoc.setDirection(direction);
        }

        return camLoc;
    }
}
