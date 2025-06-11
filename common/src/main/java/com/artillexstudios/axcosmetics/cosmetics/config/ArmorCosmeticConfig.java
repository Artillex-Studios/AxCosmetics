package com.artillexstudios.axcosmetics.cosmetics.config;

import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.utils.EquipmentSlot;
import com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfig;

import java.util.Map;

public final class ArmorCosmeticConfig extends CosmeticConfig {
    private final WrappedItemStack itemStack;

    public ArmorCosmeticConfig(Map<String, Object> config) {
        super(config);
        this.itemStack = this.get("item-stack", WrappedItemStack.class);
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

    public WrappedItemStack itemStack() {
        return this.itemStack;
    }
}
