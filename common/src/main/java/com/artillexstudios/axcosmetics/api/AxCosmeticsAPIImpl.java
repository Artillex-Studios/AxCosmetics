package com.artillexstudios.axcosmetics.api;

import com.artillexstudios.axcosmetics.AxCosmeticsPlugin;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticSlots;
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
}
