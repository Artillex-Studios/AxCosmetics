package com.artillexstudios.axcosmetics.integrations;

import com.artillexstudios.axcosmetics.api.AxCosmeticsAPI;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticSlot;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticSlots;
import com.artillexstudios.axcosmetics.api.user.User;
import com.artillexstudios.axvanish.api.event.UserVanishStateChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class AxVanishIntegration implements Listener {

    @EventHandler
    public void onUserVanishStateChangeEvent(UserVanishStateChangeEvent event) {
        User user = AxCosmeticsAPI.instance().getUserIfLoadedImmediately(event.user().player());
        if (user == null) {
            return;
        }

        if (event.newState()) {
            user.hideSlot(CosmeticSlots.BACKPACK);
        } else {
            user.showSlot(CosmeticSlots.BACKPACK);
        }
    }
}
