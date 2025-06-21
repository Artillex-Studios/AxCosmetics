package com.artillexstudios.axcosmetics.gui.actions.implementation;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcosmetics.api.user.User;
import com.artillexstudios.axcosmetics.gui.GuiBase;
import com.artillexstudios.axcosmetics.gui.actions.Action;
import org.bukkit.entity.Player;

public final class ActionMessage extends Action<String> {

    public ActionMessage() {
        super("message");
    }

    @Override
    public String evaluate(String input) {
        return input;
    }

    @Override
    public void execute(User user, GuiBase base, String value) {
        Player player = user.onlinePlayer();
        if (player == null) {
            LogUtils.warn("Tried to send message to {}, who is offline!", user.player().getName());
            return;
        }

        // TODO: parse message
        player.sendMessage(value);
    }
}
