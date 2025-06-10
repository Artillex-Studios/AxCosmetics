package com.artillexstudios.axcosmetics.api.cosmetics;

import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

public interface CosmeticSlots {

    @Nullable
    CosmeticSlot register(CosmeticSlot slot);

    void deregister(CosmeticSlot slot);

    @Nullable
    CosmeticSlot fetch(String identifier);

    Collection<CosmeticSlot> registered();

    Set<String> names();
}
