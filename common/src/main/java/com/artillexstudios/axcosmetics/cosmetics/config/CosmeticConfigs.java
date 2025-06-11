package com.artillexstudios.axcosmetics.cosmetics.config;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfig;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class CosmeticConfigs implements com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfigs {
    private final ConcurrentHashMap<String, CosmeticConfig> identifierToCosmeticConfigMap = new ConcurrentHashMap<>();

    @Override
    public void register(CosmeticConfig config) {
        if (this.identifierToCosmeticConfigMap.containsKey(config.name())) {
            LogUtils.warn("Failed to register config with identifier {} as it is already loaded!", config.name());
            return;
        }

        this.identifierToCosmeticConfigMap.put(config.name(), config);
    }

    @Override
    public void deregister(CosmeticConfig config) {
        if (!this.identifierToCosmeticConfigMap.containsKey(config.name())) {
            LogUtils.warn("Failed to deregister config with identifier {} as it is not loaded!", config.name());
            return;
        }

        this.identifierToCosmeticConfigMap.remove(config.name());
    }

    @Override
    public @Nullable CosmeticConfig fetch(String identifier) {
        return this.identifierToCosmeticConfigMap.get(identifier);
    }

    @Override
    public Collection<CosmeticConfig> registered() {
        return Collections.unmodifiableCollection(this.identifierToCosmeticConfigMap.values());
    }

    @Override
    public Set<String> names() {
        return Collections.unmodifiableSet(this.identifierToCosmeticConfigMap.keySet());
    }
}
