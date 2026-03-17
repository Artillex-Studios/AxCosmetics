package com.artillexstudios.axcosmetics.command;

public class CosmeticArgumentType {

//    public static Argument<Pair<TriFunction<User, CosmeticData, CosmeticConfig, Cosmetic<CosmeticConfig>>, CosmeticConfig>> cosmetic(String nodeName) {
//        return new CustomArgument<>(new StringArgument(nodeName), info -> {
//            CosmeticConfig fetch = AxCosmeticsAPI.instance().cosmeticConfigs().fetch(info.input());
//            if (fetch == null) {
//                throw CustomArgument.CustomArgumentException.fromAdventureComponent(StringUtils.format(Language.prefix + Language.cosmeticNotFound, Placeholder.unparsed("type", info.input())));
//            }
//
//            TriFunction<User, CosmeticData, CosmeticConfig, Cosmetic<CosmeticConfig>> fetch1 = AxCosmeticsAPI.instance().cosmeticTypes().fetch(fetch.type());
//            if (fetch1 == null) {
//                throw CustomArgument.CustomArgumentException.fromAdventureComponent(StringUtils.format(Language.prefix + Language.cosmeticNotFound, Placeholder.unparsed("type", info.input())));
//            }
//
//            return Pair.of(fetch1, fetch);
//        }).replaceSuggestions(ArgumentSuggestions.strings(info ->
//                AxCosmeticsAPI.instance().cosmeticConfigs().names().toArray(new String[0])
//        ));
//    }
}
