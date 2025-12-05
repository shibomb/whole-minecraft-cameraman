package xyz.shibomb.cameraman.targets;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class EntityTarget implements CameraTarget {
    private final Entity entity;

    public EntityTarget(Entity entity) {
        this.entity = entity;
    }

    @Override
    public Location getLocation() {
        return entity.getLocation();
    }

    @Override
    public boolean isValid() {
        return entity != null && entity.isValid();
    }

    @Override
    public String getName() {
        return entity.getName();
    }

    public Entity getEntity() {
        return entity;
    }
}
