package xyz.shibomb.cameraman.shots;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import xyz.shibomb.cameraman.targets.CameraTarget;

public interface CameraShot {
    /**
     * Calculates the next location for the camera.
     *
     * @param cameraman The player acting as the cameraman.
     * @param target    The target being spectated.
     * @param tick      The current tick of the spectate task.
     * @return The location where the camera should be.
     */
    Location getNextLocation(Player cameraman, CameraTarget target, long tick);
}
