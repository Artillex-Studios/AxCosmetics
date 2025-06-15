package com.artillexstudios.axcosmetics.api.cosmetics;

import com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfig;
import com.artillexstudios.axcosmetics.api.user.User;

import java.util.Collection;
import java.util.Objects;

/**
 * Represents a type of cosmetic.
 * This is a stateful implementation, you can have all the state
 * you want on this cosmetic.
 * @param <T> The CosmeticConfig type.
 */
public abstract class Cosmetic<T extends CosmeticConfig> {
    private final User user;
    private final T config;
    private CosmeticData data;

    public Cosmetic(User user, CosmeticData data, T config) {
        this.user = user;
        this.data = data;
        this.config = config;

        if (!this.validSlots().contains(config.slot())) {
            throw new IllegalStateException();
        }
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

    public void data(CosmeticData data) {
        this.data = data;
    }

    public CosmeticSlot slot() {
        return this.config.slot();
    }

    public abstract Collection<CosmeticSlot> validSlots();

    @Override
    public final boolean equals(Object object) {
        if (!(object instanceof Cosmetic<?> cosmetic)) {
            return false;
        }

        return Objects.equals(this.data, cosmetic.data);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.data);
    }
}
