package com.artillexstudios.axcosmetics.api.cosmetics;

import com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfig;

public record BuildableCosmeticData<T extends CosmeticConfig>(CosmeticBuilder<T> builder, CosmeticConfig config) {
}
