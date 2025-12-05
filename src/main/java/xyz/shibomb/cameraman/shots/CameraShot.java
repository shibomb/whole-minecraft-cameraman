package xyz.shibomb.cameraman.shots;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface CameraShot {
    /**
     * Calculates the next location for the camera.
     *
     * @param cameraman The player acting as the cameraman.
     * @param target    The entity being spectated.
     * @param tick      The current tick of the spectate task.
     * @return The location where the camera should be.
     */
    Location getNextLocation(Player cameraman, Entity target, long tick);
}
