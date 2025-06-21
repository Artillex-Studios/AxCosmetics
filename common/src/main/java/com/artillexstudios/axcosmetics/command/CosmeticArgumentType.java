package com.artillexstudios.axcosmetics.command;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcosmetics.api.AxCosmeticsAPI;
import com.artillexstudios.axcosmetics.api.cosmetics.Cosmetic;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticData;
import com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfig;
import com.artillexstudios.axcosmetics.api.user.User;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.apache.commons.lang3.function.TriFunction;
import org.bukkit.entity.Player;

public class CosmeticArgumentType {

    public static Argument<Cosmetic<?>> cosmetic(String nodeName) {
        return new CustomArgument<Cosmetic<?>, String>(new StringArgument(nodeName), info -> {
            User user = AxCosmeticsAPI.instance().getUserIfLoadedImmediately((Player) info.sender());
            CosmeticConfig fetch = AxCosmeticsAPI.instance().cosmeticConfigs().fetch(info.input());
            if (fetch == null) {
                LogUtils.error("No fetched with id {}", info.input());
                return null;
            }

            TriFunction<User, CosmeticData, CosmeticConfig, Cosmetic<CosmeticConfig>> fetch1 = AxCosmeticsAPI.instance().cosmeticTypes().fetch(fetch.type());
            if (fetch1 == null) {
                // TODO: Fail message
                LogUtils.error("No other fetched with id {}", fetch.type());
                return null;
            }

            return fetch1.apply(user, new CosmeticData(0, 0, 0, System.currentTimeMillis()), fetch);
        }).replaceSuggestions(ArgumentSuggestions.strings(info ->
                AxCosmeticsAPI.instance().cosmeticConfigs().names().toArray(new String[0])
        ));
    }
}
