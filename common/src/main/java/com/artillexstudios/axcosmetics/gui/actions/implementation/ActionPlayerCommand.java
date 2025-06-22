package com.artillexstudios.axcosmetics.gui.actions.implementation;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcosmetics.api.user.User;
import com.artillexstudios.axcosmetics.gui.GuiBase;
import com.artillexstudios.axcosmetics.gui.actions.Action;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public final class ActionPlayerCommand extends Action<String> {

    public ActionPlayerCommand() {
        super("player");
    }

    @Override
    public String evaluate(String input) {
        return input;
    }

    @Override
    public void execute(User user, GuiBase base, String value) {
        Player player = user.onlinePlayer();
        if (player == null) {
            LogUtils.warn("Tried to execute command as {}, who is offline!", user.player().getName());
            return;
        }

        player.performCommand(PlaceholderAPI.setPlaceholders(player, value));
    }
}
