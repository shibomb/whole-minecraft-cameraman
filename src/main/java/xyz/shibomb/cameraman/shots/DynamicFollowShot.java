package xyz.shibomb.cameraman.shots;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import xyz.shibomb.cameraman.SpectatePerspective;
import xyz.shibomb.cameraman.targets.CameraTarget;

public class DynamicFollowShot implements CameraShot {

    private final double distance;
    private final double height;
    private final double smoothness;
    private final SpectatePerspective perspective;

    public DynamicFollowShot(double distance, double height, double smoothness, SpectatePerspective perspective) {
        this.distance = distance;
        this.height = height;
        this.smoothness = Math.max(0.01, Math.min(1.0, smoothness));
        this.perspective = perspective;
    }

    @Override
    public Location getNextLocation(Player cameraman, CameraTarget target, long tick) {
        Location targetLoc = target.getLocation();
        Vector direction = targetLoc.getDirection().setY(0).normalize(); // Horizontal direction

        Location idealLoc;

        if (perspective == SpectatePerspective.DYNAMIC_FRONT || perspective == SpectatePerspective.FRONT) {
            // FRONT: Target + Direction * Distance
            idealLoc = targetLoc.clone().add(direction.clone().multiply(distance));
            idealLoc.add(0, height, 0);
            // Look AT target (Inverse of direction)
            idealLoc.setDirection(targetLoc.clone().subtract(idealLoc).toVector());

        } else if (perspective == SpectatePerspective.DYNAMIC_POV || perspective == SpectatePerspective.POV) {
            // POV: Eye location
            // Use actual entity eye location if available
            if (target instanceof xyz.shibomb.cameraman.targets.EntityTarget) {
                org.bukkit.entity.Entity entity = ((xyz.shibomb.cameraman.targets.EntityTarget) target).getEntity();
                if (entity instanceof org.bukkit.entity.LivingEntity) {
                    idealLoc = ((org.bukkit.entity.LivingEntity) entity).getEyeLocation();
                } else {
                    idealLoc = targetLoc.clone().add(0, 1.62, 0); // Approximate if not living
                }
            } else {
                idealLoc = targetLoc.clone().add(0, 1.62, 0); // Location target fallback
            }

            // NOTE: We do NOT add this.height for POV.

            // CRITICAL FIX: The idealLoc is where we want the CAMERA to be.
            // But we are teleporting the player's FEET to this location.
            // The player's camera is at FEET + EYE_HEIGHT.
            // So we must subtract the cameraman's eye height from the idealLoc.
            idealLoc.subtract(0, cameraman.getEyeHeight(), 0);

            // Add small offset to be "above head" (approx 0.5 blocks) to avoid clipping
            // inside the model
            idealLoc.add(0, 0.5, 0);

            // Look same direction as target
            idealLoc.setDirection(targetLoc.getDirection());

        } else {
            // BEHIND (Default)
            // Target - Direction * Distance
            idealLoc = targetLoc.clone().subtract(direction.multiply(distance));
            idealLoc.add(0, height, 0);
            // Look at target
            idealLoc.setDirection(targetLoc.clone().subtract(idealLoc).toVector());
        }

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
