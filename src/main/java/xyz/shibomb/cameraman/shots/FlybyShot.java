package xyz.shibomb.cameraman.shots;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class FlybyShot implements CameraShot {

    private final long durationTicks;
    // Offsets are relative to the target's position AND rotation.
    // X = Left/Right, Y = Up/Down, Z = Front/Back
    // BUT typically we just want world-relative or simple directional offsets.
    // Let's use simple relative-to-target-direction offsets.
    private final Vector startOffset;
    private final Vector endOffset;

    public FlybyShot(double durationSeconds) {
        this.durationTicks = (long) (durationSeconds * 20);
        // Hardcoded offsets for now, could be passed in
        // Start: Front-Left, Low
        this.startOffset = new Vector(3, 1, 3);
        // End: Back-Right, High
        this.endOffset = new Vector(-3, 5, -3);
    }

    // Allow custom offsets
    public FlybyShot(double durationSeconds, Vector startOffset, Vector endOffset) {
        this.durationTicks = (long) (durationSeconds * 20);
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    @Override
    public Location getNextLocation(Player cameraman, Entity target, long tick) {
        long cycleTick = tick % (durationTicks * 2);
        double t;

        if (cycleTick < durationTicks) {
            // Forward: 0 -> 1
            t = (double) cycleTick / durationTicks;
        } else {
            // Backward: 1 -> 0
            t = 1.0 - ((double) (cycleTick - durationTicks) / durationTicks);
        }

        // Easing (SmoothStep)
        t = t * t * (3 - 2 * t);

        Location targetLoc = target.getLocation();

        // We need to calculate world-space offsets based on target's rotation if we
        // want them "Relative"
        // But for a simple Flyby, maybe just World offsets + Target Position is enough?
        // Let's try Relative to View Direction.

        Vector direction = targetLoc.getDirection().setY(0).normalize();
        Vector right = direction.clone().crossProduct(new Vector(0, 1, 0)).normalize();

        // Calculate World Vector for Start Offset
        // x is right, z is backward (standard Minecraft direction usually z is forward,
        // but lets define our offset meaning)
        // Let's say: X+ is Right, Y+ is Up, Z+ is Forward (Direction).

        Vector startWorldVec = getRelativeVector(direction, right, startOffset);
        Vector endWorldVec = getRelativeVector(direction, right, endOffset);

        // Lerp between startWorld and endWorld
        Vector currentOffset = startWorldVec.clone().add(endWorldVec.clone().subtract(startWorldVec).multiply(t));

        Location camLoc = targetLoc.clone().add(currentOffset);

        // Always look at target
        camLoc.setDirection(targetLoc.subtract(camLoc).toVector());

        return camLoc;
    }

    private Vector getRelativeVector(Vector forward, Vector right, Vector offset) {
        // offset.x * right + offset.y * Up + offset.z * forward
        return right.clone().multiply(offset.getX())
                .add(new Vector(0, offset.getY(), 0))
                .add(forward.clone().multiply(offset.getZ()));
    }
}
