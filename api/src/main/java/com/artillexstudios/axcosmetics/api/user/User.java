package com.artillexstudios.axcosmetics.api.user;

import com.artillexstudios.axcosmetics.api.cosmetics.Cosmetic;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticSlot;
import com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfig;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
    default <T extends CosmeticConfig> Cosmetic<T> getCosmetic(CosmeticSlot slot) {
        return this.getCosmetic(slot, false);
    }

    @Nullable
    <T extends CosmeticConfig> Cosmetic<T> getCosmetic(CosmeticSlot slot, boolean owned);

    /**
     * Adds a cosmetic to a user.
     * @param cosmetic The cosmetic to add
     * @param <T> The CosmeticConfig type of the cosmetic
     */
    <T extends CosmeticConfig> CompletableFuture<?> addCosmetic(Cosmetic<T> cosmetic);

    /**
     * Delete a cosmetic from a user.
     * @param cosmetic The cosmetic to delete
     * @param <T> The CosmeticConfig type of the cosmetic
     */
    <T extends CosmeticConfig> CompletableFuture<?> deleteCosmetic(Cosmetic<T> cosmetic);

    /**
     * Updates the cosmetic of a user in a slot.
     * @param slot The slot to update.
     */
    void updateCosmetic(CosmeticSlot slot);

    Collection<Cosmetic<?>> getCosmetics();

    Collection<? extends Cosmetic<?>> getEquippedCosmetics();

    /**
     * Equip a cosmetic for a user.
     * The user doesn't need to have this cosmetic.
     * @param cosmetic The cosmetic to equip.
     */
    void equipCosmetic(Cosmetic<?> cosmetic);

    boolean unequipCosmetic(Cosmetic<?> cosmetic);

    boolean unequipCosmetic(CosmeticSlot slot);

    <T extends CosmeticConfig> boolean isEquipped(Cosmetic<T> cosmetic);
}
