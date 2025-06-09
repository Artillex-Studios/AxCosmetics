package com.artillexstudios.axcosmetics.cosmetics.config;

import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfig;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

// TODO: Better configs
public class FirstPersonBackpackConfig extends CosmeticConfig {
    private double height = 1.5d;
    private WrappedItemStack itemStack = WrappedItemStack.wrap(new ItemStack(Material.AIR));
    private WrappedItemStack firstPersonItemStack = WrappedItemStack.wrap(new ItemStack(Material.AIR));;

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
