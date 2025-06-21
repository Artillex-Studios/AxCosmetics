package com.artillexstudios.axcosmetics.cosmetics.config;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfig;
import com.artillexstudios.axcosmetics.database.DatabaseAccessor;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class CosmeticConfigs implements com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfigs {
    private final ConcurrentHashMap<String, CosmeticConfig> identifierToCosmeticConfigMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, CosmeticConfig> idToCosmeticConfigMap = new ConcurrentHashMap<>();
    private final DatabaseAccessor accessor;

    public CosmeticConfigs(DatabaseAccessor accessor) {
        this.accessor = accessor;
    }

    @Override
    public CompletableFuture<?> register(CosmeticConfig config) {
        // Prevent the registration of the same cosmetic config, while one is currently being registered
        this.identifierToCosmeticConfigMap.put(config.name(), config);
        return this.accessor.registerCosmeticConfig(config).thenAccept(id -> {
            this.identifierToCosmeticConfigMap.remove(config.name());
            if (id == null) {
                return;
            }

            config.id(id);
            this.register(config.name(), id, config);
        });
    }

    public <T extends CosmeticConfig> void register(String identifier, int id, T config) {
        if (this.identifierToCosmeticConfigMap.containsKey(identifier)) {
            LogUtils.warn("Failed to register cosmetic config with identifier {} as it is already loaded!", identifier);
            return;
        }

        this.identifierToCosmeticConfigMap.put(identifier, config);
        this.idToCosmeticConfigMap.put(id, config);
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
    public @Nullable CosmeticConfig fetch(int identifier) {
        return this.idToCosmeticConfigMap.get(identifier);
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
