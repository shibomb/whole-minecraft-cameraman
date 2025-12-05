package xyz.shibomb.cameraman.shots;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import xyz.shibomb.cameraman.targets.CameraTarget;

public class DynamicFollowShot implements CameraShot {

    private final double distance;
    private final double height;
    private final double smoothness;

    public DynamicFollowShot(double distance, double height, double smoothness) {
        this.distance = distance;
        this.height = height;
        this.smoothness = Math.max(0.01, Math.min(1.0, smoothness));
    }

    @Override
    public Location getNextLocation(Player cameraman, CameraTarget target, long tick) {
        // Ideal position is standard BEHIND logic
        Location targetLoc = target.getLocation();
        Vector direction = targetLoc.getDirection().setY(0).normalize(); // Horizontal direction
        Location idealLoc = targetLoc.clone().subtract(direction.multiply(distance));
        idealLoc.add(0, height, 0);

        // Orient to look at target
        idealLoc.setDirection(targetLoc.clone().subtract(idealLoc).toVector());

        // If it's the first tick, jump to ideal immediately to avoid flying in from
        // spawn
        if (tick == 0) {
            return idealLoc;
        }

        // Get current camera location
        Location currentLoc = cameraman.getLocation();

        // Lerp position
        Vector currentVec = currentLoc.toVector();
        Vector idealVec = idealLoc.toVector();
        Vector newVec = currentVec.clone().add(idealVec.subtract(currentVec).multiply(smoothness));

        Location newLoc = newVec.toLocation(currentLoc.getWorld());

        // Interpolate Yaw/Pitch for smoothness
        float newYaw = lerpAngle(currentLoc.getYaw(), idealLoc.getYaw(), smoothness);
        float newPitch = lerp(currentLoc.getPitch(), idealLoc.getPitch(), smoothness);

        newLoc.setYaw(newYaw);
        newLoc.setPitch(newPitch);

        return newLoc;
    }

    private float lerp(float start, float end, double t) {
        return (float) (start + (end - start) * t);
    }

    private float lerpAngle(float start, float end, double t) {
        float diff = end - start;
        while (diff < -180F)
            diff += 360F;
        while (diff >= 180F)
            diff -= 360F;
        return (float) (start + diff * t);
    }
}
