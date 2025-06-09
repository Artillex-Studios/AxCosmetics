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
    private boolean spawned = false;

    public Cosmetic(User user, CosmeticData data, T config) {
        this.user = user;
        this.data = data;
        this.config = config;
    }

    public abstract void update();

    public void spawn() {
        this.spawned = true;
    }

    public void despawn() {
        this.spawned = false;
    }

    public boolean spawned() {
        return this.spawned;
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

    public abstract Collection<Class<? extends CosmeticSlot>> validSlots();
}
