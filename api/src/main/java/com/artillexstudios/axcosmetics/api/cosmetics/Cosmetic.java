package com.artillexstudios.axcosmetics.api.cosmetics;

import com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfig;
import com.artillexstudios.axcosmetics.api.user.User;

import java.util.Collection;

/**
 * Represents a type of cosmetic.
 * This is a stateful implementation, you can have all the state
 * you want on this cosmetic.
 * @param <T> The CosmeticConfig type.
 */
public abstract class Cosmetic<T extends CosmeticConfig> {
    private final User user;
    private final CosmeticData data;
    private final T config;

    public Cosmetic(User user, CosmeticData data, T config) {
        this.user = user;
        this.data = data;
        this.config = config;
    }

    public abstract void update();

    public void spawn() {

    }

    public void despawn() {

    }

    public T config() {
        return this.config;
    }

    public User user() {
        return this.user;
    }

    public CosmeticData data() {
        return this.data;
    }

    public CosmeticSlot slot() {
        return this.config.slot();
    }

    public abstract Collection<CosmeticSlot> validSlots();
}
