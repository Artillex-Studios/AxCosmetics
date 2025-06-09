package com.artillexstudios.axcosmetics.listener;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcosmetics.api.exception.UserAlreadyLoadedException;
import com.artillexstudios.axcosmetics.user.UserRepository;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerListener implements Listener {
    private final UserRepository repository;

    public PlayerListener(UserRepository repository) {
        this.repository = repository;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        try {
            this.repository.loadUser(event.getPlayer().getUniqueId());
        } catch (UserAlreadyLoadedException exception) {
            LogUtils.error("Failed to load already loaded user {}!", event.getPlayer().getName(), exception);
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        // TODO: remove cosmetics
        this.repository.disconnect(event.getPlayer().getUniqueId());
    }
}
