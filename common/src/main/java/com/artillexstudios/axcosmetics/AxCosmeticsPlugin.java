package com.artillexstudios.axcosmetics;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.database.DatabaseHandler;
import com.artillexstudios.axapi.database.DatabaseTypes;
import com.artillexstudios.axapi.database.impl.H2DatabaseType;
import com.artillexstudios.axapi.database.impl.MySQLDatabaseType;
import com.artillexstudios.axapi.database.impl.SQLiteDatabaseType;
import com.artillexstudios.axapi.dependencies.DependencyManagerWrapper;
import com.artillexstudios.axapi.gui.configuration.actions.Actions;
import com.artillexstudios.axapi.metrics.AxMetrics;
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
import com.artillexstudios.axcosmetics.cosmetics.CosmeticTicker;
import com.artillexstudios.axcosmetics.cosmetics.CosmeticTypes;
import com.artillexstudios.axcosmetics.cosmetics.config.ArmorCosmeticConfig;
import com.artillexstudios.axcosmetics.cosmetics.config.BackpackConfig;
import com.artillexstudios.axcosmetics.cosmetics.config.CosmeticConfigLoader;
import com.artillexstudios.axcosmetics.cosmetics.config.CosmeticConfigTypes;
import com.artillexstudios.axcosmetics.cosmetics.config.CosmeticConfigs;
import com.artillexstudios.axcosmetics.cosmetics.config.FirstPersonBackpackConfig;
import com.artillexstudios.axcosmetics.cosmetics.type.ArmorCosmetic;
import com.artillexstudios.axcosmetics.cosmetics.type.InteractionFirstPersonBackpackCosmetic;
import com.artillexstudios.axcosmetics.cosmetics.type.LegacyFirstPersonBackpackCosmetic;
import com.artillexstudios.axcosmetics.cosmetics.type.NonFirstPersonBackpackCosmetic;
import com.artillexstudios.axcosmetics.database.DatabaseAccessor;
import com.artillexstudios.axcosmetics.entitymeta.InteractionMeta;
import com.artillexstudios.axcosmetics.gui.ActionUnequip;
import com.artillexstudios.axcosmetics.integrations.AxVanishIntegration;
import com.artillexstudios.axcosmetics.listener.ArmorCosmeticListener;
import com.artillexstudios.axcosmetics.listener.BackpackCosmeticListener;
import com.artillexstudios.axcosmetics.listener.CosmeticPacketListener;
import com.artillexstudios.axcosmetics.listener.PlayerListener;
import com.artillexstudios.axcosmetics.listener.RidePacketListener;
import com.artillexstudios.axcosmetics.user.UserRepository;
import com.artillexstudios.axcosmetics.utils.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;

public final class AxCosmeticsPlugin extends AxPlugin {
    private static AxCosmeticsPlugin instance;
    private AxMetrics metrics;
    private UserRepository userRepository;
    private CosmeticConfigTypes cosmeticConfigTypes;
    private CosmeticTypes cosmeticTypes;
    private CosmeticConfigLoader configLoader;
    private CosmeticConfigs cosmeticConfigs;
    private DatabaseHandler handler;
    private CosmeticTicker ticker;
    private CosmeticSlots slots;

    @Override
    public void dependencies(DependencyManagerWrapper manager) {
        manager.dependency("com{}h2database:h2:2.3.232");
        manager.dependency("dev{}jorel:commandapi-bukkit-shade:10.1.0", true);

        manager.relocate("org{}h2", "com.artillexstudios.axcosmetics.libs.h2");
        manager.relocate("dev{}jorel{}commandapi", "com.artillexstudios.axcosmetics.libs.commandapi");
    }

    @Override
    public void updateFlags() {
        FeatureFlags.PACKET_ENTITY_TRACKER_ENABLED.set(true);
        FeatureFlags.DEBUG.set(true);
    }

    @Override
    public void load() {
        instance = this;

        Actions.INSTANCE.register("unequip", ActionUnequip::new);
        FileUtils.copyFromResource("cosmetics");
        DatabaseTypes.register(new H2DatabaseType("com.artillexstudios.axcosmetics.libs.h2"), true);
        DatabaseTypes.register(new SQLiteDatabaseType());
        DatabaseTypes.register(new MySQLDatabaseType());

        Config.reload();
        Language.reload();
        AsyncUtils.setup(Config.asyncProcessorPoolSize);

        Config.database.tablePrefix(Config.tablePrefix);
        this.handler = new DatabaseHandler(this, Config.database);
        DatabaseAccessor accessor = new DatabaseAccessor(this.handler);
        this.userRepository = new UserRepository(accessor);
        this.slots = new CosmeticSlots();
        this.cosmeticConfigs = new CosmeticConfigs(accessor);
        this.cosmeticConfigTypes = new CosmeticConfigTypes();
        this.cosmeticTypes = new CosmeticTypes();
        this.configLoader = new CosmeticConfigLoader();
        this.ticker = new CosmeticTicker();
        accessor.create();

        FeatureFlags.PACKET_ENTITY_TRACKER_THREADS.set(3); // TODO: Maybe configurable?
        EntityMetaFactory.register(EntityType.INTERACTION, InteractionMeta::new);
        AxCosmeticsCommand.load(this);

        AxCosmeticsAPI.instance().cosmeticSlots().register(new CosmeticSlot("helmet"));
        AxCosmeticsAPI.instance().cosmeticSlots().register(new CosmeticSlot("chest_plate"));
        AxCosmeticsAPI.instance().cosmeticSlots().register(new CosmeticSlot("leggings"));
        AxCosmeticsAPI.instance().cosmeticSlots().register(new CosmeticSlot("boots"));
        AxCosmeticsAPI.instance().cosmeticSlots().register(new CosmeticSlot("main_hand"));
        AxCosmeticsAPI.instance().cosmeticSlots().register(new CosmeticSlot("off_hand"));
        AxCosmeticsAPI.instance().cosmeticSlots().register(new CosmeticSlot("backpack"));

        AxCosmeticsAPI.instance().cosmeticConfigTypes().register("first-person-backpack", FirstPersonBackpackConfig::new);
        AxCosmeticsAPI.instance().cosmeticConfigTypes().register("legacy-first-person-backpack", FirstPersonBackpackConfig::new);
        AxCosmeticsAPI.instance().cosmeticConfigTypes().register("backpack", BackpackConfig::new);
        AxCosmeticsAPI.instance().cosmeticConfigTypes().register("armor", ArmorCosmeticConfig::new);

        AxCosmeticsAPI.instance().cosmeticTypes().register("first-person-backpack", InteractionFirstPersonBackpackCosmetic::new);
        AxCosmeticsAPI.instance().cosmeticTypes().register("legacy-first-person-backpack", LegacyFirstPersonBackpackCosmetic::new);
        AxCosmeticsAPI.instance().cosmeticTypes().register("backpack", NonFirstPersonBackpackCosmetic::new);
        AxCosmeticsAPI.instance().cosmeticTypes().register("armor", ArmorCosmetic::new);
    }

    @Override
    public void enable() {
        this.ticker.start();
        this.configLoader.loadAll();
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this.userRepository), this);
        Bukkit.getPluginManager().registerEvents(new ArmorCosmeticListener(), this);
        Bukkit.getPluginManager().registerEvents(new BackpackCosmeticListener(), this);
        if (Bukkit.getPluginManager().getPlugin("AxVanish") != null) {
            Bukkit.getPluginManager().registerEvents(new AxVanishIntegration(), this);
        }

        AxCosmeticsCommand.register();
        AxCosmeticsCommand.enable();
        PacketEvents.INSTANCE.addListener(new CosmeticPacketListener());
        if (Config.listenToRidePackets) {
            PacketEvents.INSTANCE.addListener(new RidePacketListener());
        }
        this.metrics = new AxMetrics(this, 48);
        this.metrics.start();
    }

    @Override
    public void disable() {
        this.ticker.cancel();
        AxCosmeticsCommand.disable();
        AsyncUtils.stop();
        this.handler.close();
        this.metrics.cancel();
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

    public CosmeticConfigTypes cosmeticConfigTypes() {
        return this.cosmeticConfigTypes;
    }

    public CosmeticTypes cosmeticTypes() {
        return this.cosmeticTypes;
    }

    public CosmeticConfigLoader configLoader() {
        return this.configLoader;
    }

    public DatabaseHandler handler() {
        return this.handler;
    }
}