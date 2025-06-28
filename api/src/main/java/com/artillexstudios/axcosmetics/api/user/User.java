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


    /**
     * Gets the equipped cosmetic of the user from the given slot.
     * @param slot The CosmeticSlot to check
     * @return The equipped cosmetic
     * @param <T> The CosmeticConfig type of the cosmetic
     */
    @Nullable
    default <T extends CosmeticConfig> Cosmetic<T> getCosmetic(CosmeticSlot slot) {
        return this.getCosmetic(slot, false);
    }


    /**
     * Gets the equipped cosmetic of the user from the given slot, according to the owned
     * boolean parameter. If owned is set to true, it returns the cosmetic which is owned by the player, and not
     * equipped by other means.
     * @param slot The CosmeticSlot to check
     * @param owned If we should check for cosmetic ownership
     * @return The equipped cosmetic
     * @param <T> The CosmeticConfig type of the cosmetic
     */
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

    /**
     * @return An immutable copy of the user's cosmetics.
     */
    Collection<Cosmetic<?>> getCosmetics();

    /**
     * @return An immutable copy of the user's cosmetics.
     */
    Collection<? extends Cosmetic<?>> getEquippedCosmetics();

    /**
     * Equip a cosmetic for a user.
     * The user doesn't need to have this cosmetic.
     * @param cosmetic The cosmetic to equip.
     */
    void equipCosmetic(Cosmetic<?> cosmetic);

    /**
     * Unequips a cosmetic from the user. If this cosmetic could not
     * be unequipped, or the user has a different cosmetic equipped, the method returns false.
     * @param cosmetic The cosmetic to unequip
     * @return If the process was successful true, else false
     */
    boolean unequipCosmetic(Cosmetic<?> cosmetic);

    /**
     * Unequips the cosmetic of the user in the slot.
     * @param slot The CosmeticSlot to unequip
     * @return If the process was successful true, else false
     */
    boolean unequipCosmetic(CosmeticSlot slot);

    /**
     * Check if the user has this cosmetic equipped.
     * @param cosmetic The cosmetic to check
     * @return If the usre has this cosmetic equipped.
     * @param <T> The CosmeticConfig type of the cosmetic
     */
    <T extends CosmeticConfig> boolean isEquipped(Cosmetic<T> cosmetic);

    /**
     * Hides the slot of the user. This is a permit based system.
     * @param slot The slot to hide
     */
    void hideSlot(CosmeticSlot slot);

    /**
     * Shows the cosmetic in slot.
     * @param slot The slot to show
     * @throws IllegalStateException If the cosmetic is already shown.
     */
    void showSlot(CosmeticSlot slot) throws IllegalStateException;

    /**
     * Checks if the slot is hidden.
     * @param slot The slot to check
     * @return If the slot is hidden
     */
    boolean isSlotHidden(CosmeticSlot slot);

    /**
     * Checks if the user has a cosmetic with this config.
     * @param config The CosmeticConfig to check.
     * @return Whether the player has a cosmetic with this config.
     */
    boolean has(CosmeticConfig config);

    /**
     * Returns a list of cosmetics from the user matching a CosmeticConfig.
     * @param config The config to check.
     * @return A List containing the cosmetics matching the config.
     */
    List<Cosmetic<?>> matching(CosmeticConfig config);

    /**
     * Updates the permission based cosmetics of the user.
     */
    void updatePermissionCosmetics();
}
