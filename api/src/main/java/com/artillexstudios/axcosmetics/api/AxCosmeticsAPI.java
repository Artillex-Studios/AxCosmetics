package com.artillexstudios.axcosmetics.api;

import com.artillexstudios.axcosmetics.api.user.UserRepository;
import net.kyori.adventure.util.Services;

public interface AxCosmeticsAPI {

    static AxCosmeticsAPI instance() {
        return Holder.INSTANCE;
    }

    UserRepository userRepository();

    final class Holder {
        private static final AxCosmeticsAPI INSTANCE = Services.service(AxCosmeticsAPI.class).orElseThrow();
    }
}