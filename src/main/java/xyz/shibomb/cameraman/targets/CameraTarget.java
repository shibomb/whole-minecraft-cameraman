package xyz.shibomb.cameraman.targets;

import org.bukkit.Location;

public interface CameraTarget {
    Location getLocation();

    boolean isValid();

    String getName();
}
