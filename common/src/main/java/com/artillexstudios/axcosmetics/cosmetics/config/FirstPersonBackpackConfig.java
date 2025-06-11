package com.artillexstudios.axcosmetics.cosmetics.config;

import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfig;

import java.util.Map;

public final class FirstPersonBackpackConfig extends CosmeticConfig {
    private final double height;
    private final WrappedItemStack itemStack;
    private final WrappedItemStack firstPersonItemStack;

    public FirstPersonBackpackConfig(String name, Map<String, Object> config) {
        super(name, config);
        this.height = this.getDouble("height");
        this.itemStack = this.get("item-stack", WrappedItemStack.class);
        this.firstPersonItemStack = this.get("first-person-item-stack", WrappedItemStack.class);
    }

    public double height() {
        return this.height;
    }

    public WrappedItemStack itemStack() {
        return this.itemStack;
    }

    public WrappedItemStack firstPersonItemStack() {
        return this.firstPersonItemStack;
    }
}
