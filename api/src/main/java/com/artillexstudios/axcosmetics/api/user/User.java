package com.artillexstudios.axcosmetics.api.user;

import com.artillexstudios.axcosmetics.api.cosmetics.Cosmetic;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticSlot;
import com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfig;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

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

    @Nullable
    <T extends CosmeticConfig> Cosmetic<T> getCosmetic(CosmeticSlot slot);

    <T extends CosmeticConfig> void addCosmetic(Cosmetic<T> cosmetic);

    void updateCosmetic(CosmeticSlot slot);

    Collection<Cosmetic<?>> getCosmetics();

    Collection<Cosmetic<?>> getEquippedCosmetics();

    void equipCosmetic(Cosmetic<?> cosmetic);

    boolean unequipCosmetic(Cosmetic<?> cosmetic);

    boolean unequipCosmetic(CosmeticSlot slot);
}
