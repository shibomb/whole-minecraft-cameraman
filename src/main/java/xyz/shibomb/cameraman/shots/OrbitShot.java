package xyz.shibomb.cameraman.shots;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import xyz.shibomb.cameraman.targets.CameraTarget;

public class OrbitShot implements CameraShot {

    private final double radius;
    private final double speed; // Degrees per tick
    private final double height;
    private double currentAngle = 0;

    public OrbitShot(double radius, double speed, double height) {
        this.radius = radius;
        this.speed = speed;
        this.height = height;
    }

    @Override
    public Location getNextLocation(Player cameraman, CameraTarget target, long tick) {
        Location targetLoc = target.getLocation();

        // Update angle
        currentAngle += speed;
        if (currentAngle >= 360)
            currentAngle -= 360;

        double radians = Math.toRadians(currentAngle);
        double x = Math.cos(radians) * radius;
        double z = Math.sin(radians) * radius;

        Location camLoc = targetLoc.clone().add(x, height, z);

        // Look at target's head
        Location lookAt = targetLoc.clone().add(0, 1.5, 0);
        camLoc.setDirection(lookAt.toVector().subtract(camLoc.toVector()));

        return camLoc;
    }
}
