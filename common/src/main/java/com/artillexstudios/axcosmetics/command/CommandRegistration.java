package com.artillexstudios.axcosmetics.command;

import com.artillexstudios.axcosmetics.api.AxCosmeticsAPI;
import com.artillexstudios.axcosmetics.api.cosmetics.BuildableCosmeticData;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticBuilder;
import com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfig;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import revxrsal.commands.exception.CommandErrorException;

public enum CommandRegistration {
    INSTANCE;
    private BukkitCommandHandler handler;

    public void register(JavaPlugin plugin) {
        this.handler = BukkitCommandHandler.create(plugin);

        this.handler.registerValueResolver(BuildableCosmeticData.class, ctx -> {
            CosmeticConfig config = AxCosmeticsAPI.instance().cosmeticConfigs().fetch(ctx.popForParameter());
            if (config == null) {
                throw new CommandErrorException("Couldn't find CosmeticType named " + ctx.popForParameter() + "!");
            }

            CosmeticBuilder<CosmeticConfig> builder = AxCosmeticsAPI.instance().cosmeticTypes().fetch(config.type());
            if (builder == null) {
                // TODO: Localize
                throw new CommandErrorException("Couldn't find CosmeticBuilder named " + config.type() + "!");
            }

            return new BuildableCosmeticData<>(builder, config);
        });

        this.handler.getAutoCompleter().registerParameterSuggestions(BuildableCosmeticData.class, (args, actor, command) -> {
            return AxCosmeticsAPI.instance().cosmeticConfigs().names();
        });

        this.handler.register(new AxCosmeticsCommand());
        this.handler.registerBrigadier();
    }
}
