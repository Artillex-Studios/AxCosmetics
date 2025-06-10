package com.artillexstudios.axcosmetics.cosmetics;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticSlot;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class CosmeticSlots implements com.artillexstudios.axcosmetics.api.cosmetics.CosmeticSlots {
    private final ConcurrentHashMap<String, CosmeticSlot> identifierToCosmeticSlotMap = new ConcurrentHashMap<>();

    @Override
    public CosmeticSlot register(CosmeticSlot slot) {
        if (this.identifierToCosmeticSlotMap.containsKey(slot.name())) {
            LogUtils.warn("Failed to register slot with identifier {} as it is already loaded!", slot.name());
            return null;
        }

        this.identifierToCosmeticSlotMap.put(slot.name(), slot);
        return slot;
    }

    @Override
    public void deregister(CosmeticSlot slot) {
        if (!this.identifierToCosmeticSlotMap.containsKey(slot.name())) {
            LogUtils.warn("Failed to deregister slot with identifier {} as it is not loaded!", slot.name());
            return;
        }

        this.identifierToCosmeticSlotMap.remove(slot.name());
    }

    @Override
    public @Nullable CosmeticSlot fetch(String identifier) {
        return this.identifierToCosmeticSlotMap.get(identifier);
    }

    @Override
    public Collection<CosmeticSlot> registered() {
        return Collections.unmodifiableCollection(this.identifierToCosmeticSlotMap.values());
    }

    @Override
    public Set<String> names() {
        return Collections.unmodifiableSet(this.identifierToCosmeticSlotMap.keySet());
    }
}
