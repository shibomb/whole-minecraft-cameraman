package xyz.shibomb.cameraman;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final CameramanManager manager;

    public PlayerJoinListener(CameramanManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        manager.restoreCameraman(event.getPlayer());
        
        Player cameraman = manager.getCameraman();
        if (cameraman != null && cameraman.getUniqueId().equals(event.getPlayer().getUniqueId())) {
            return; // Don't target self
        }

        if (manager.isNewcomerMode()) {
            manager.setTarget(event.getPlayer());
        }
    }
}
