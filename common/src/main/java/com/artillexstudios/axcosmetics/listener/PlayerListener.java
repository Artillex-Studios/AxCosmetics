package com.artillexstudios.axcosmetics.listener;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcosmetics.api.cosmetics.Cosmetic;
import com.artillexstudios.axcosmetics.api.exception.UserAlreadyLoadedException;
import com.artillexstudios.axcosmetics.api.user.User;
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
            this.repository.loadUser(event.getPlayer().getUniqueId()).thenAccept(user -> {
                ((com.artillexstudios.axcosmetics.user.User) user).onlinePlayer(event.getPlayer());
                for (Cosmetic<?> cosmetic : user.getEquippedCosmetics()) {
                    cosmetic.spawn();
                }
            });
        } catch (UserAlreadyLoadedException exception) {
            LogUtils.error("Failed to load already loaded user {}!", event.getPlayer().getName(), exception);
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        User user = this.repository.disconnect(event.getPlayer().getUniqueId());
        for (Cosmetic<?> equippedCosmetic : user.getEquippedCosmetics()) {
            equippedCosmetic.despawn();
        }
        ((com.artillexstudios.axcosmetics.user.User) user).onlinePlayer(null);
    }
}
