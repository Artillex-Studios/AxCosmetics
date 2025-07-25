package com.artillexstudios.axcosmetics.api.cosmetics.config;

import com.artillexstudios.axapi.config.adapters.ConfigurationGetter;
import com.artillexstudios.axapi.config.adapters.TypeAdapterHolder;
import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axcosmetics.api.AxCosmeticsAPI;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticData;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticSlot;
import com.artillexstudios.axcosmetics.api.exception.MissingConfigurationOptionException;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a possible configuration for a cosmetic.
 * These could also be called CosmeticTypes, but I think the CosmeticConfig
 * represents what they do better.
 */
public abstract class CosmeticConfig implements ConfigurationGetter {
    private final TypeAdapterHolder holder = new TypeAdapterHolder();
    private final String name;
    private final String permission;
    private final Map<String, Object> config;
    private final CosmeticSlot slot;
    private final String type;
    private Integer id;

    public CosmeticConfig(String name, Map<String, Object> config) throws MissingConfigurationOptionException {
        this.name = name;
        this.config = config;
        this.slot = this.getCosmeticSlot("slot");
        this.type = this.getString("type");
        this.permission = this.getNullable("permission", String.class);
    }

    public CosmeticSlot getCosmeticSlot(String path) {
        String identifier = this.getString(path);

        CosmeticSlot slot = AxCosmeticsAPI.instance().cosmeticSlots().fetch(identifier.toLowerCase(Locale.ENGLISH));
        if (slot == null) {
            throw new IllegalArgumentException("Invalid slot!");
        }

        return slot;
    }

    @Override
    public <T> T get(String path, Class<T> clazz) throws MissingConfigurationOptionException {
        T value = this.getNullable(path, clazz);
        if (value == null) {
            throw new MissingConfigurationOptionException(path);
        }

        return value;
    }

    public <T> T getNullable(String path, Class<T> clazz) {
        return clazz.cast(this.holder.deserialize(this.config.get(path), clazz));
    }

    public CosmeticSlot slot() {
        return this.slot;
    }

    public String name() {
        return this.name;
    }

    public String type() {
        return this.type;
    }

    public int id() {
        return this.id;
    }

    public abstract WrappedItemStack guiItem(CosmeticData data);

    public void id(int id) {
        if (this.id != null) {
            throw new IllegalStateException();
        }

        this.id = id;
    }

    public String permission() {
        return this.permission;
    }

    @Override
    public final boolean equals(Object object) {
        if (!(object instanceof CosmeticConfig that)) {
            return false;
        }

        return Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.name);
    }
}
