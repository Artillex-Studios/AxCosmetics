package com.artillexstudios.axcosmetics.listener;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcosmetics.api.AxCosmeticsAPI;
import com.artillexstudios.axcosmetics.api.cosmetics.Cosmetic;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticData;
import com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfig;
import com.artillexstudios.axcosmetics.api.exception.UserAlreadyLoadedException;
import com.artillexstudios.axcosmetics.api.user.User;
import com.artillexstudios.axcosmetics.config.Config;
import com.artillexstudios.axcosmetics.user.UserRepository;
import org.apache.commons.lang3.function.TriFunction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public final class PlayerListener implements Listener {
    private final UserRepository repository;

    public PlayerListener(UserRepository repository) {
        this.repository = repository;
    }

    @EventHandler
    public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
        try {
            this.repository.loadUser(event.getUniqueId()).join();
        } catch (UserAlreadyLoadedException exception) {
            LogUtils.error("Failed to load already loaded user {}!", event.getName(), exception);
        } catch (Throwable throwable) {
            LogUtils.error("A different error occurred while loading user {}!", event.getName(), throwable);
        }
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        User user = AxCosmeticsAPI.instance().getUserIfLoadedImmediately(event.getPlayer());
        user.updatePermissionCosmetics();

        for (Cosmetic<?> cosmetic : user.getEquippedCosmetics()) {
            if (Config.debug) {
                LogUtils.debug("Spawning equipped cosmetic {}", cosmetic.data());
            }
            cosmetic.spawn();
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        User user = this.repository.disconnect(event.getPlayer().getUniqueId());
        for (Cosmetic<?> equippedCosmetic : user.getEquippedCosmetics()) {
            if (Config.debug) {
                LogUtils.debug("Despawning equipped cosmetic {}", equippedCosmetic.data());
            }
            equippedCosmetic.despawn();
        }
        ((com.artillexstudios.axcosmetics.user.User) user).onlinePlayer(null);
    }
}
