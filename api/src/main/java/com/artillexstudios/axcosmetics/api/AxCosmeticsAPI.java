package com.artillexstudios.axcosmetics.api;

import com.artillexstudios.axcosmetics.api.cosmetics.Cosmetic;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticData;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticSlots;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticTypes;
import com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfig;
import com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfigTypes;
import com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfigs;
import com.artillexstudios.axcosmetics.api.user.User;
import com.artillexstudios.axcosmetics.api.user.UserRepository;
import net.kyori.adventure.util.Services;
import org.apache.commons.lang3.function.TriFunction;
import org.bukkit.OfflinePlayer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface AxCosmeticsAPI {

    /**
     * Get the instance of the API.
     * @return The API instance.
     */
    static AxCosmeticsAPI instance() {
        return Holder.INSTANCE;
    }

    /**
     * Creates a cosmetic with the provided data.
     * @param user The User to create the Cosmetic for.
     * @param cosmeticTypeId The id of the cosmetic's type.
     * @param data The CosmeticData to create the cosmetic with.
     * @return The Cosmetic.
     */
    default Cosmetic<CosmeticConfig> createCosmetic(User user, int cosmeticTypeId, CosmeticData data) {
        CosmeticConfig config = AxCosmeticsAPI.instance().cosmeticConfigs().fetch(cosmeticTypeId);
        if (config == null) {
            return null;
        }

        TriFunction<User, CosmeticData, CosmeticConfig, Cosmetic<CosmeticConfig>> function = AxCosmeticsAPI.instance().cosmeticTypes().fetch(config.type());
        if (function == null) {
            return null;
        }

        return function.apply(user, data, config);
    }

    /**
     * Get a user from a player if loaded, without blocking.
     * @param player The player to get the user of.
     * @return The User object connected to this player.
     */
    default User getUserIfLoadedImmediately(OfflinePlayer player) {
        return this.getUserIfLoadedImmediately(player.getUniqueId());
    }

    /**
     * Get a user from a player if loaded, without blocking.
     * @param uuid The uuid to get the user of.
     * @return The User object connected to this player.
     */
    default User getUserIfLoadedImmediately(UUID uuid) {
        return this.userRepository().getUserIfLoadedImmediately(uuid);
    }

    /**
     * Get a user from a player if loaded, without blocking.
     * @param entityId The entityId to get the user of.
     * @return The User object connected to this player.
     */
    default User getUserIfLoadedImmediately(int entityId) {
        return this.userRepository().getUserIfLoadedImmediately(entityId);
    }

    /**
     * Get a user from a player.
     * @param player The player to get the user of.
     * @return A CompletableFuture of the user.
     */
    default CompletableFuture<User> getUser(OfflinePlayer player) {
        return this.getUser(player.getUniqueId());
    }

    /**
     * Get a user from a player.
     * @param uuid The UUID to get the user of.
     * @return A CompletableFuture of the user.
     */
    default CompletableFuture<User> getUser(UUID uuid) {
        return this.userRepository().getUser(uuid, LoadContext.TEMPORARY);
    }

    UserRepository userRepository();

    CosmeticSlots cosmeticSlots();

    CosmeticConfigs cosmeticConfigs();

    CosmeticConfigTypes cosmeticConfigTypes();

    CosmeticTypes cosmeticTypes();

    final class Holder {
        private static final AxCosmeticsAPI INSTANCE = Services.service(AxCosmeticsAPI.class).orElseThrow();
    }
}