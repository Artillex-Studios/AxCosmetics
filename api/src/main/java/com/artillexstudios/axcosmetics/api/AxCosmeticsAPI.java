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

    static AxCosmeticsAPI instance() {
        return Holder.INSTANCE;
    }

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

    default User getUserIfLoadedImmediately(OfflinePlayer player) {
        return this.getUserIfLoadedImmediately(player.getUniqueId());
    }

    default User getUserIfLoadedImmediately(UUID uuid) {
        return this.userRepository().getUserIfLoadedImmediately(uuid);
    }

    default User getUserIfLoadedImmediately(int entityId) {
        return this.userRepository().getUserIfLoadedImmediately(entityId);
    }

    default CompletableFuture<User> getUser(OfflinePlayer player) {
        return this.getUser(player.getUniqueId());
    }

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