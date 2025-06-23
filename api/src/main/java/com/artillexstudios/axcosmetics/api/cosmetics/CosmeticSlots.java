package com.artillexstudios.axcosmetics.api.cosmetics;

import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

public interface CosmeticSlots {
    CosmeticSlot HELMET = new CosmeticSlot("helmet");
    CosmeticSlot CHEST_PLATE = new CosmeticSlot("chest_plate");
    CosmeticSlot LEGGINGS = new CosmeticSlot("leggings");
    CosmeticSlot BOOTS = new CosmeticSlot("boots");
    CosmeticSlot MAIN_HAND = new CosmeticSlot("main_hand");
    CosmeticSlot OFF_HAND = new CosmeticSlot("off_hand");
    CosmeticSlot BACKPACK = new CosmeticSlot("backpack");

    @Nullable
    CosmeticSlot register(CosmeticSlot slot);

    void deregister(CosmeticSlot slot);

    @Nullable
    CosmeticSlot fetch(String identifier);

    Collection<CosmeticSlot> registered();

    Set<String> names();
}
