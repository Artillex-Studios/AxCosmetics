package com.artillexstudios.axcosmetics.cosmetics.config;

import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.utils.EquipmentSlot;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticData;
import com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfig;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

public final class ArmorCosmeticConfig extends CosmeticConfig {

    public ArmorCosmeticConfig(String name, Map<String, Object> config) {
        super(name, config);
    }

    @Override
    public WrappedItemStack guiItem(CosmeticData data) {
        return this.itemStack(data);
    }

    public EquipmentSlot equipmentSlot() {
        return switch (this.slot().name()) {
            case "helmet" -> EquipmentSlot.HELMET;
            case "chest_plate" -> EquipmentSlot.CHEST_PLATE;
            case "leggings" -> EquipmentSlot.LEGGINGS;
            case "boots" -> EquipmentSlot.BOOTS;
            case "main_hand" -> EquipmentSlot.MAIN_HAND;
            case "off_hand" -> EquipmentSlot.OFF_HAND;
            case null, default -> throw new IllegalStateException();
        };
    }

    public WrappedItemStack itemStack(CosmeticData data) {
        return new ItemBuilder(this.getMap("item-stack"),
                Placeholder.unparsed("edition", String.valueOf(data.counter())),
                Formatter.date("date", ZonedDateTime.ofInstant(Instant.ofEpochMilli(data.timeStamp()), ZoneId.systemDefault()))
        ).wrapped();
    }
}
