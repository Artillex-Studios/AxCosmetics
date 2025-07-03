package com.artillexstudios.axcosmetics.cosmetics.config;

import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticData;
import com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfig;
import com.artillexstudios.axcosmetics.api.exception.MissingConfigurationOptionException;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

public final class BackpackConfig extends CosmeticConfig {
    private final WrappedItemStack itemStack;

    public BackpackConfig(String name, Map<String, Object> config) throws MissingConfigurationOptionException {
        super(name, config);
        this.itemStack = new ItemBuilder((Map<Object, Object>) this.getMap("item-stack")).wrapped();
    }

    @Override
    public WrappedItemStack guiItem(CosmeticData data) {
        return new ItemBuilder((Map<Object, Object>) this.getMap("item-stack"),
                Placeholder.unparsed("edition", String.valueOf(data.counter())),
                Formatter.date("date", ZonedDateTime.ofInstant(Instant.ofEpochMilli(data.timeStamp()), ZoneId.systemDefault()))
        ).wrapped();
    }

    public WrappedItemStack itemStack() {
        return this.itemStack;
    }
}
