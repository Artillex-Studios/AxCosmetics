package com.artillexstudios.axcosmetics;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.database.DatabaseHandler;
import com.artillexstudios.axapi.database.DatabaseTypes;
import com.artillexstudios.axapi.database.impl.H2DatabaseType;
import com.artillexstudios.axapi.database.impl.MySQLDatabaseType;
import com.artillexstudios.axapi.database.impl.SQLiteDatabaseType;
import com.artillexstudios.axapi.dependencies.DependencyManagerWrapper;
import com.artillexstudios.axapi.packetentity.meta.EntityMetaFactory;
import com.artillexstudios.axapi.utils.AsyncUtils;
import com.artillexstudios.axapi.utils.featureflags.FeatureFlags;
import com.artillexstudios.axcosmetics.command.AxCosmeticsCommand;
import com.artillexstudios.axcosmetics.config.Config;
import com.artillexstudios.axcosmetics.config.Language;
import com.artillexstudios.axcosmetics.database.DatabaseAccessor;
import com.artillexstudios.axcosmetics.entitymeta.InteractionMeta;
import com.artillexstudios.axcosmetics.user.UserRepository;
import org.bukkit.entity.EntityType;

public final class AxCosmeticsPlugin extends AxPlugin {
    private static AxCosmeticsPlugin instance;
    private UserRepository userRepository;

    @Override
    public void dependencies(DependencyManagerWrapper manager) {
        // TODO: Load dependencies
    }

    @Override
    public void updateFlags() {
        FeatureFlags.PACKET_ENTITY_TRACKER_ENABLED.set(true);
    }

    @Override
    public void load() {
        instance = this;

        DatabaseTypes.register(new H2DatabaseType("com.artillexstudios.axcosmetics.libs.h2"), true);
        DatabaseTypes.register(new SQLiteDatabaseType());
        DatabaseTypes.register(new MySQLDatabaseType());

        Config.reload();
        Language.reload();
        this.userRepository = new UserRepository(new DatabaseAccessor(new DatabaseHandler(this, Config.database)));
        AsyncUtils.setup(Config.asyncProcessorPoolSize);
        Config.database.tablePrefix(Config.tablePrefix);

        FeatureFlags.PACKET_ENTITY_TRACKER_THREADS.set(3); // TODO: Maybe configurable?
        EntityMetaFactory.register(EntityType.INTERACTION, InteractionMeta::new);
        AxCosmeticsCommand.load(this);
    }

    @Override
    public void enable() {
        AxCosmeticsCommand.register();
        AxCosmeticsCommand.enable();
    }

    @Override
    public void disable() {
        AxCosmeticsCommand.disable();
    }

    public static AxCosmeticsPlugin instance() {
        return instance;
    }

    public UserRepository userRepository() {
        return this.userRepository;
    }
}