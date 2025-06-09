package com.artillexstudios.axcosmetics.api.user;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

public interface User {

    /**
     * Get the id of the user.
     * @return The user's id.
     */
    int id();

    /**
     * Get the OfflinePlayer associated with this player.
     * @return An OfflinePlayer
     */
    OfflinePlayer player();

    /**
     * Get the online Player associated with this player.
     * @return A Player, or null, if the player is not online
     */
    @Nullable
    Player onlinePlayer();
}
