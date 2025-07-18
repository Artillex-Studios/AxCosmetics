package com.artillexstudios.axcosmetics.cosmetics.config;

import com.artillexstudios.axapi.utils.UncheckedUtils;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfig;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public final class CosmeticConfigTypes implements com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfigTypes {
    private final ConcurrentHashMap<String, BiFunction<String, Map<String, Object>, ?>> identifierToCosmeticConfigTypeMap = new ConcurrentHashMap<>();

    @Override
    public <T extends CosmeticConfig> void register(String identifier, BiFunction<String, Map<String, Object>, T> cosmeticConfigClass) {
        if (this.identifierToCosmeticConfigTypeMap.containsKey(identifier)) {
            LogUtils.warn("Failed to register config type with identifier {} as it is already loaded!", identifier);
            return;
        }

        this.identifierToCosmeticConfigTypeMap.put(identifier, cosmeticConfigClass);
    }

    @Override
    public void deregister(String identifier) {
        if (!this.identifierToCosmeticConfigTypeMap.containsKey(identifier)) {
            LogUtils.warn("Failed to deregister config with identifier {} as it is not loaded!", identifier);
            return;
        }

        this.identifierToCosmeticConfigTypeMap.remove(identifier);
    }

    @Override
    public <T extends CosmeticConfig> @Nullable BiFunction<String, Map<String, Object>, T> fetch(String identifier) {
        return UncheckedUtils.unsafeCast(this.identifierToCosmeticConfigTypeMap.get(identifier));
    }

    @Override
    public Collection<BiFunction<String, Map<String, Object>, ?>> registered() {
        return Collections.unmodifiableCollection(this.identifierToCosmeticConfigTypeMap.values());
    }

    @Override
    public Set<String> names() {
        return Collections.unmodifiableSet(this.identifierToCosmeticConfigTypeMap.keySet());
    }
}
