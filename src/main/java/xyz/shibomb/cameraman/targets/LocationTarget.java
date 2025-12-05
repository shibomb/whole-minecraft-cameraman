package xyz.shibomb.cameraman.targets;

import org.bukkit.Location;

public class LocationTarget implements CameraTarget {
    private final Location location;

    public LocationTarget(Location location) {
        this.location = location;
    }

    @Override
    public Location getLocation() {
        // Return a clone so the original isn't modified
        return location.clone();
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public String getName() {
        return "Location(" + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + ")";
    }
}
