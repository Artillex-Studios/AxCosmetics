package com.artillexstudios.axcosmetics.api.cosmetics;

import com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfig;
import com.artillexstudios.axcosmetics.api.user.User;

public interface CosmeticBuilder<T extends CosmeticConfig> {

    Cosmetic<T> apply(User user, CosmeticData data, T config);
}
