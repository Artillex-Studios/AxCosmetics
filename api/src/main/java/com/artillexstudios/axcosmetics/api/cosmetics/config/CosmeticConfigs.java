package com.artillexstudios.axcosmetics.api.cosmetics.config;

import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

public interface CosmeticConfigs {

    void register(CosmeticConfig config);

    void deregister(CosmeticConfig config);

    @Nullable
    CosmeticConfig fetch(String identifier);

    Collection<CosmeticConfig> registered();

    Set<String> names();
}
