package xyz.shibomb.cameraman.shots;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import xyz.shibomb.cameraman.targets.CameraTarget;

public class MoveShot implements CameraShot {

    private final double relativeX;
    private final double relativeY;
    private final double relativeZ;
    private final double speed;
    private final double distance;
    private final double height;

    public MoveShot(double x, double y, double z, double speed, double distance, double height) {
        this.relativeX = x;
        this.relativeY = y;
        this.relativeZ = z;
        this.speed = speed;
        this.distance = distance;
        this.height = height;
    }

    @Override
    public Location getNextLocation(Player cameraman, CameraTarget target, long tick) {
        Location baseLoc = target.getLocation();

        // Apply Offset (Distance & Height)
        // Similar to StaticShot BEHIND logic:
        // Height is Y offset.
        // Distance is backward offset along view direction.
        Vector forwardKey = baseLoc.getDirection();
        // Since forwardKey can point down/up, "Distance" usually means horizontal
        // distance?
        // Or strictly linear back?
        // In "Scenic" defaults, distance is often spherical or horizontal.
        // Let's use simple logic: Move BACK along the view vector by distance.
        // And UP by height (global Y).

        // If distance is used, we subtract forward * distance
        // But if forward points UP (looking up), back points DOWN.
        // If we want "Behind", we usually mean "Behind in XZ plane".
        // Let's stick to simple "Back along view vector" for full 3D functionality.
        Vector startOffset = forwardKey.clone().multiply(-distance); // Backwards
        startOffset.add(new Vector(0, height, 0)); // Up

        Location startLoc = baseLoc.clone().add(startOffset);
        // Also ensure startLoc preserves the original direction? Yes, clone does that.
        // But if we moved back, we are now looking parallel to original view.
        // Which is usually correct for "Move" shot setup.

        // Calculate Basis Vectors (using baseLoc or startLoc? baseLoc direction is the
        // reference)
        // startLoc has same direction as baseLoc.
        // So we can use startLoc for calculations.

        // 2. Horizontal Right (X)
        double yawRad = Math.toRadians(startLoc.getYaw() + 90);
        double rx = -Math.sin(yawRad);
        double rz = Math.cos(yawRad);
        Vector rightKey = new Vector(rx, 0, rz).normalize();

        // 3. Global Up (Y)
        Vector upKey = new Vector(0, 1, 0);

        // Combine inputs
        Vector moveDir = rightKey.clone().multiply(relativeX)
                .add(upKey.clone().multiply(relativeY))
                .add(forwardKey.clone().multiply(relativeZ));

        // Normalize and scale
        if (moveDir.lengthSquared() > 0) {
            moveDir.normalize().multiply(speed);
        }

        // Apply displacement over time
        Vector displacement = moveDir.clone().multiply(tick);
        Location camLoc = startLoc.clone().add(displacement);

        return camLoc;
    }
}
