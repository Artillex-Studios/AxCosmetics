package com.artillexstudios.axcosmetics.cosmetics;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcosmetics.api.cosmetics.Cosmetic;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticData;
import com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfig;
import com.artillexstudios.axcosmetics.api.user.User;
import org.apache.commons.lang3.function.TriFunction;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CosmeticTypes implements com.artillexstudios.axcosmetics.api.cosmetics.CosmeticTypes {
    private final ConcurrentHashMap<String, TriFunction<User, CosmeticData, ?, Cosmetic<?>>> identifierToCosmeticTypeMap = new ConcurrentHashMap<>();

    @Override
    public <T extends CosmeticConfig> void register(String identifier, TriFunction<User, CosmeticData, T, Cosmetic<T>> function) {
        if (this.identifierToCosmeticTypeMap.containsKey(identifier)) {
            LogUtils.warn("Failed to register cosmetic type with identifier {} as it is already loaded!", identifier);
            return;
        }

        this.identifierToCosmeticTypeMap.put(identifier, (TriFunction<User, CosmeticData, ?, Cosmetic<?>>) (Object) function);
    }

    @Override
    public void deregister(String identifier) {
        if (!this.identifierToCosmeticTypeMap.containsKey(identifier)) {
            LogUtils.warn("Failed to deregister cosmetic type with identifier {} as it is not loaded!", identifier);
            return;
        }

        this.identifierToCosmeticTypeMap.remove(identifier);
    }

    @Override
    public @Nullable <T extends CosmeticConfig> TriFunction<User, CosmeticData, T, Cosmetic<T>> fetch(String identifier) {
        return (TriFunction<User, CosmeticData, T, Cosmetic<T>>) (Object) this.identifierToCosmeticTypeMap.get(identifier);
    }

    @Override
    public Collection<TriFunction<User, CosmeticData, ?, Cosmetic<?>>> registered() {
        return Collections.unmodifiableCollection(this.identifierToCosmeticTypeMap.values());
    }

    @Override
    public Set<String> names() {
        return Collections.unmodifiableSet(this.identifierToCosmeticTypeMap.keySet());
    }
}
