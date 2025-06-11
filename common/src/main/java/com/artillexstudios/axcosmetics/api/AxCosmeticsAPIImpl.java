package com.artillexstudios.axcosmetics.api;

import com.artillexstudios.axcosmetics.AxCosmeticsPlugin;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticSlots;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticTypes;
import com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfigTypes;
import com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfigs;
import com.artillexstudios.axcosmetics.api.user.UserRepository;

public final class AxCosmeticsAPIImpl implements AxCosmeticsAPI {

    @Override
    public UserRepository userRepository() {
        return AxCosmeticsPlugin.instance().userRepository();
    }

    @Override
    public CosmeticSlots cosmeticSlots() {
        return AxCosmeticsPlugin.instance().slots();
    }

    @Override
    public CosmeticConfigs cosmeticConfigs() {
        return AxCosmeticsPlugin.instance().cosmeticConfigs();
    }

    @Override
    public CosmeticConfigTypes cosmeticConfigTypes() {
        return AxCosmeticsPlugin.instance().cosmeticConfigTypes();
    }

    @Override
    public CosmeticTypes cosmeticTypes() {
        return AxCosmeticsPlugin.instance().cosmeticTypes();
    }
}
