package com.artillexstudios.axcosmetics.listener;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcosmetics.api.AxCosmeticsAPI;
import com.artillexstudios.axcosmetics.api.user.User;
import com.artillexstudios.axcosmetics.config.Config;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public final class ArmorCosmeticListener implements Listener {

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            if (Config.debug) {
                LogUtils.debug("PlayerInteractEvent - Not right click!");
            }
            return;
        }

        if (event.getItem() == null) {
            if (Config.debug) {
                LogUtils.debug("PlayerInteractEvent - Empty hand!");
            }
            return;
        }

        User user = AxCosmeticsAPI.instance().getUserIfLoadedImmediately(event.getPlayer().getUniqueId());
        if (user == null) {
            if (Config.debug) {
                LogUtils.debug("PlayerInteractEvent - user null");
            }
            return;
        }

        CosmeticPacketListener.sendUserArmorUpdate(user, event.getPlayer());
        if (Config.debug) {
            LogUtils.debug("PlayerInteractEvent - Armor update");
        }
    }
}
