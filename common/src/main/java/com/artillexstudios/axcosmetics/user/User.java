package com.artillexstudios.axcosmetics.user;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

public class User implements com.artillexstudios.axcosmetics.api.user.User {

    @Override
    public int id() {
        return 0;
    }

    @Override
    public OfflinePlayer player() {
        return null;
    }

    @Override
    public @Nullable Player onlinePlayer() {
        return null;
    }
}
