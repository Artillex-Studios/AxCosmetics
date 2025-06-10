package com.artillexstudios.axcosmetics.api.cosmetics.config;

import com.artillexstudios.axapi.config.adapters.ConfigurationGetter;
import com.artillexstudios.axapi.config.adapters.TypeAdapterHolder;
import com.artillexstudios.axcosmetics.api.AxCosmeticsAPI;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticSlot;

import java.util.Map;

public abstract class CosmeticConfig implements ConfigurationGetter {
    private final TypeAdapterHolder holder = new TypeAdapterHolder();
    private final Map<String, Object> config;
    private final CosmeticSlot slot;

    public CosmeticConfig(Map<String, Object> config) {
        this.config = config;
        this.slot = this.getCosmeticSlot("slot");
    }

    public CosmeticSlot getCosmeticSlot(String path) {
        String identifier = this.getString(path);
        if (identifier == null) {
            throw new IllegalArgumentException("No slot field!");
        }

        CosmeticSlot slot = AxCosmeticsAPI.instance().cosmeticSlots().fetch(identifier);
        if (slot == null) {
            throw new IllegalArgumentException("Invalid slot!");
        }

        return slot;
    }

    @Override
    public <T> T get(String path, Class<T> clazz) {
        return clazz.cast(this.holder.deserialize(this.config.get(path), clazz));
    }

    public CosmeticSlot slot() {
        return this.slot;
    }
}
