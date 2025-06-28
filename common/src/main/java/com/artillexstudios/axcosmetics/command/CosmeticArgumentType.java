package com.artillexstudios.axcosmetics.command;

import com.artillexstudios.axapi.utils.Pair;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axcosmetics.api.AxCosmeticsAPI;
import com.artillexstudios.axcosmetics.api.cosmetics.Cosmetic;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticData;
import com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfig;
import com.artillexstudios.axcosmetics.api.user.User;
import com.artillexstudios.axcosmetics.config.Language;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.apache.commons.lang3.function.TriFunction;

public class CosmeticArgumentType {

    public static Argument<Pair<TriFunction<User, CosmeticData, CosmeticConfig, Cosmetic<CosmeticConfig>>, CosmeticConfig>> cosmetic(String nodeName) {
        return new CustomArgument<>(new StringArgument(nodeName), info -> {
            CosmeticConfig fetch = AxCosmeticsAPI.instance().cosmeticConfigs().fetch(info.input());
            if (fetch == null) {
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(StringUtils.format(Language.prefix + Language.cosmeticNotFound, Placeholder.unparsed("type", info.input())));
            }

            TriFunction<User, CosmeticData, CosmeticConfig, Cosmetic<CosmeticConfig>> fetch1 = AxCosmeticsAPI.instance().cosmeticTypes().fetch(fetch.type());
            if (fetch1 == null) {
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(StringUtils.format(Language.prefix + Language.cosmeticNotFound, Placeholder.unparsed("type", info.input())));
            }

            return Pair.of(fetch1, fetch);
        }).replaceSuggestions(ArgumentSuggestions.strings(info ->
                AxCosmeticsAPI.instance().cosmeticConfigs().names().toArray(new String[0])
        ));
    }
}
