package com.artillexstudios.axcosmetics.listener;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axcosmetics.api.AxCosmeticsAPI;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticSlots;
import com.artillexstudios.axcosmetics.api.user.User;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public final class BackpackCosmeticListener implements Listener {

    @EventHandler
    public void onPlayerGameModeChangeEvent(PlayerGameModeChangeEvent event) {
        User user = AxCosmeticsAPI.instance().getUserIfLoadedImmediately(event.getPlayer().getUniqueId());
        if (user == null) {
            return;
        }

        if (event.getNewGameMode() == GameMode.SPECTATOR) {
            user.hideSlot(CosmeticSlots.BACKPACK);
        } else if (user.isSlotHidden(CosmeticSlots.BACKPACK)) {
            user.showSlot(CosmeticSlots.BACKPACK);
        }
    }

    @EventHandler
    public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
        User user = AxCosmeticsAPI.instance().getUserIfLoadedImmediately(event.getPlayer().getUniqueId());
        if (user == null) {
            return;
        }

        user.hideSlot(CosmeticSlots.BACKPACK);
        Scheduler.get().runLater(() -> {
            user.showSlot(CosmeticSlots.BACKPACK);
        }, 1L);
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        User user = AxCosmeticsAPI.instance().getUserIfLoadedImmediately(event.getEntity().getUniqueId());
        if (user == null) {
            return;
        }

        user.hideSlot(CosmeticSlots.BACKPACK);
    }

    @EventHandler
    public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
        User user = AxCosmeticsAPI.instance().getUserIfLoadedImmediately(event.getPlayer().getUniqueId());
        if (user == null) {
            return;
        }

        user.showSlot(CosmeticSlots.BACKPACK);
    }
}
