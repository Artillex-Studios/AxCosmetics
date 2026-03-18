package com.artillexstudios.axcosmetics.cosmetics;

import com.artillexstudios.axapi.utils.UncheckedUtils;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcosmetics.api.cosmetics.Cosmetic;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticBuilder;
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
    private final ConcurrentHashMap<String, CosmeticBuilder<?>> identifierToCosmeticTypeMap = new ConcurrentHashMap<>();

    @Override
    public <T extends CosmeticConfig> void register(String identifier, CosmeticBuilder<T> function) {
        if (this.identifierToCosmeticTypeMap.containsKey(identifier)) {
            LogUtils.warn("Failed to register cosmetic type with identifier {} as it is already loaded!", identifier);
            return;
        }

        this.identifierToCosmeticTypeMap.put(identifier, UncheckedUtils.unsafeCast(function));
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
    public @Nullable <T extends CosmeticConfig> CosmeticBuilder<T> fetch(String identifier) {
        return UncheckedUtils.unsafeCast(this.identifierToCosmeticTypeMap.get(identifier));
    }

    @Override
    public Collection<CosmeticBuilder<?>> registered() {
        return Collections.unmodifiableCollection(this.identifierToCosmeticTypeMap.values());
    }

    @Override
    public Set<String> names() {
        return Collections.unmodifiableSet(this.identifierToCosmeticTypeMap.keySet());
    }
}
