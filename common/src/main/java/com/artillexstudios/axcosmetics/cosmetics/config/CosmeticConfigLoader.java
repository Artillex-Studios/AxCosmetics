package com.artillexstudios.axcosmetics.cosmetics.config;

import com.artillexstudios.axapi.config.YamlConfiguration;
import com.artillexstudios.axapi.utils.YamlUtils;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcosmetics.api.AxCosmeticsAPI;
import com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfig;
import com.artillexstudios.axcosmetics.api.exception.MissingConfigurationOptionException;
import com.artillexstudios.axcosmetics.config.Config;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public final class CosmeticConfigLoader {

    public CompletableFuture<?> loadAll() {
        Collection<File> files = FileUtils.listFiles(com.artillexstudios.axcosmetics.utils.FileUtils.PLUGIN_DIRECTORY.resolve("cosmetics/").toFile(), new String[]{"yml", "yaml"}, true);
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (File file : files) {
            if (!YamlUtils.suggest(file)) {
                continue;
            }

            YamlConfiguration<?> configuration = YamlConfiguration.of(file.toPath()).build();
            configuration.load();

            for (String key : configuration.keys()) {
                String type = configuration.getString(key + ".type");
                if (type == null || type.isBlank()) {
                    LogUtils.warn("Failed to construct cosmetic from key {} due to missing type!", key);
                    continue;
                }

                BiFunction<String, Map<String, Object>, CosmeticConfig> cosmeticSupplier = AxCosmeticsAPI.instance().cosmeticConfigTypes().fetch(type);
                if (cosmeticSupplier == null) {
                    LogUtils.warn("Failed to construct cosmetic from key {} due to invalid cosmetic type! Found: {}, valid values: {}", key, type, AxCosmeticsAPI.instance().cosmeticConfigTypes().names());
                    continue;
                }

                try {
                    Map<String, Object> map = (Map<String, Object>) configuration.getMap(key);
                    CosmeticConfig cosmeticConfig = cosmeticSupplier.apply(type, map);
                    if (Config.debug) {
                        LogUtils.debug("Loading cosmetic config {} from data: {}!", key, map);
                    }

                    futures.add(AxCosmeticsAPI.instance().cosmeticConfigs().register(cosmeticConfig));
                } catch (MissingConfigurationOptionException exception) {
                    LogUtils.warn("Failed to load cosmetic from key {} due to a missing option!", key, exception.option());
                }
            }
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }
}
