package com.artillexstudios.axcosmetics.cosmetics.config;

import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticData;
import com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfig;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

public final class FirstPersonBackpackConfig extends CosmeticConfig {
    private final double height;
    private final WrappedItemStack firstPersonItemStack;
    private final WrappedItemStack itemStack;

    public FirstPersonBackpackConfig(String name, Map<String, Object> config) {
        super(name, config);
        this.height = this.getDouble("height");
        this.firstPersonItemStack = this.get("first-person-item-stack", WrappedItemStack.class);
        this.itemStack = ItemBuilder.create(this.getMap("item-stack")).wrapped();
    }

    @Override
    public WrappedItemStack guiItem(CosmeticData data) {
        return ItemBuilder.create(this.getMap("item-stack"),
                Placeholder.unparsed("edition", String.valueOf(data.counter())),
                Formatter.date("date", ZonedDateTime.ofInstant(Instant.ofEpochMilli(data.timeStamp()), ZoneId.systemDefault()))
        ).wrapped();
    }

    public double height() {
        return this.height;
    }

    public WrappedItemStack firstPersonItemStack() {
        return this.firstPersonItemStack;
    }

    public WrappedItemStack itemStack() {
        return this.itemStack;
    }
}
