package com.artillexstudios.axcosmetics;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.database.DatabaseHandler;
import com.artillexstudios.axapi.database.DatabaseTypes;
import com.artillexstudios.axapi.database.impl.H2DatabaseType;
import com.artillexstudios.axapi.database.impl.MySQLDatabaseType;
import com.artillexstudios.axapi.database.impl.SQLiteDatabaseType;
import com.artillexstudios.axapi.dependencies.DependencyManagerWrapper;
import com.artillexstudios.axapi.packet.PacketEvents;
import com.artillexstudios.axapi.packetentity.meta.EntityMetaFactory;
import com.artillexstudios.axapi.utils.AsyncUtils;
import com.artillexstudios.axapi.utils.featureflags.FeatureFlags;
import com.artillexstudios.axcosmetics.api.AxCosmeticsAPI;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticSlot;
import com.artillexstudios.axcosmetics.command.AxCosmeticsCommand;
import com.artillexstudios.axcosmetics.config.Config;
import com.artillexstudios.axcosmetics.config.Language;
import com.artillexstudios.axcosmetics.cosmetics.CosmeticSlots;
import com.artillexstudios.axcosmetics.cosmetics.config.CosmeticConfigs;
import com.artillexstudios.axcosmetics.database.DatabaseAccessor;
import com.artillexstudios.axcosmetics.entitymeta.InteractionMeta;
import com.artillexstudios.axcosmetics.listener.ArmorCosmeticPacketListener;
import com.artillexstudios.axcosmetics.listener.PlayerListener;
import com.artillexstudios.axcosmetics.user.UserRepository;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;

public final class AxCosmeticsPlugin extends AxPlugin {
    private static AxCosmeticsPlugin instance;
    private UserRepository userRepository;
    private CosmeticConfigs cosmeticConfigs;
    private CosmeticSlots slots;

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

//        DatabaseTypes.register(new H2DatabaseType("com.artillexstudios.axcosmetics.libs.h2"), true);
        DatabaseTypes.register(new SQLiteDatabaseType(), true);
        DatabaseTypes.register(new MySQLDatabaseType());

        Config.reload();
        Language.reload();
        this.userRepository = new UserRepository(new DatabaseAccessor(new DatabaseHandler(this, Config.database)));
        this.slots = new CosmeticSlots();
        this.cosmeticConfigs = new CosmeticConfigs();
        AsyncUtils.setup(Config.asyncProcessorPoolSize);
        Config.database.tablePrefix(Config.tablePrefix);

        FeatureFlags.PACKET_ENTITY_TRACKER_THREADS.set(3); // TODO: Maybe configurable?
        EntityMetaFactory.register(EntityType.INTERACTION, InteractionMeta::new);
        AxCosmeticsCommand.load(this);

        CosmeticSlot HELMET = AxCosmeticsAPI.instance().cosmeticSlots().register(new CosmeticSlot("helmet"));
        CosmeticSlot CHEST_PLATE = AxCosmeticsAPI.instance().cosmeticSlots().register(new CosmeticSlot("chest_plate"));
        CosmeticSlot LEGGINGS = AxCosmeticsAPI.instance().cosmeticSlots().register(new CosmeticSlot("leggings"));
        CosmeticSlot BOOTS = AxCosmeticsAPI.instance().cosmeticSlots().register(new CosmeticSlot("boots"));
        CosmeticSlot MAIN_HAND = AxCosmeticsAPI.instance().cosmeticSlots().register(new CosmeticSlot("main_hand"));
        CosmeticSlot OFF_HAND = AxCosmeticsAPI.instance().cosmeticSlots().register(new CosmeticSlot("off_hand"));
        CosmeticSlot BACKPACK = AxCosmeticsAPI.instance().cosmeticSlots().register(new CosmeticSlot("backpack"));
    }

    @Override
    public void enable() {
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this.userRepository), this);

        AxCosmeticsCommand.register();
        AxCosmeticsCommand.enable();
        PacketEvents.INSTANCE.addListener(new ArmorCosmeticPacketListener());
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

    public CosmeticSlots slots() {
        return this.slots;
    }

    public CosmeticConfigs cosmeticConfigs() {
        return this.cosmeticConfigs;
    }
}