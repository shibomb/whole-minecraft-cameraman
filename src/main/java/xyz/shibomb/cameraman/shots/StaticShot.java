package xyz.shibomb.cameraman.shots;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import xyz.shibomb.cameraman.SpectatePerspective;

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
    public Location getNextLocation(Player cameraman, Entity target, long tick) {
        Location targetLoc = target.getLocation();
        Location camLoc = targetLoc.clone();

        if (perspective == SpectatePerspective.BEHIND) {
            // Position behind the target
            Vector direction = targetLoc.getDirection().normalize().multiply(-distance);
            camLoc.add(direction);
            camLoc.add(0, height, 0); // height adjustments
            camLoc.setDirection(targetLoc.toVector().subtract(camLoc.toVector()));
        } else if (perspective == SpectatePerspective.FRONT) {
            // Position in front of the target
            Vector direction = targetLoc.getDirection().normalize().multiply(distance);
            camLoc.add(direction);
            camLoc.add(0, height, 0);

            // Face the target
            Location lookAt = targetLoc.clone().add(0, 1.5, 0); // Look at head
            camLoc.setDirection(lookAt.toVector().subtract(camLoc.toVector()));
        } else {
            // Fallback for POV or other invalid types, though POV is usually handled by
            // setSpectatorTarget
            return targetLoc;
        }

        return camLoc;
    }
}
