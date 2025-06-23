package com.artillexstudios.axcosmetics.config;

import com.artillexstudios.axapi.config.YamlConfiguration;
import com.artillexstudios.axapi.config.annotation.Comment;
import com.artillexstudios.axapi.config.annotation.ConfigurationPart;
import com.artillexstudios.axapi.database.DatabaseConfig;
import com.artillexstudios.axapi.libs.snakeyaml.DumperOptions;
import com.artillexstudios.axapi.utils.YamlUtils;
import com.artillexstudios.axcosmetics.utils.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;

public final class Config implements ConfigurationPart {
    private static final Config INSTANCE = new Config();
    public static DatabaseConfig database = new DatabaseConfig();

    @Comment("""
            This setting controls how often the plugin will resend
            armor cosmetics, if they have been marked as needing a resend.
            This is the amount of ticks, by the tick frequency that need to pass.
            Setting this to a larger number will make the cosmetic update less often, thus
            it can disappear from the player. Setting it to a larger number will also
            decrease the amount of packets sent.
            """)
    public static int armorResendFrequency = 2;
    @Comment("""
            If the plugin should listen to ride packets.
            This means, that if a different plugin is also adding
            passengers to the players, we will also add our entity into the list,
            so the other plugin doesn't interfere with our passengers.
            This has a minimal impact on the performance of the plugin
            """)
    public static boolean listenToRidePackets = true;
    @Comment("""
            If the plugin should force the riding packets.
            This means, that the backpacks will never bug off the back of the player
            but it also means that more packets will be sent, causing larger network usage.
            Only enable this if your backpacks are falling off all the time.
            """)
    public static boolean forceRidePackets = false;
    @Comment("""
            The tick frequency in milliseconds. This setting
            controls how often cosmetics will be updated.
            By default, this is 50ms = 1 tick.
            """)
    public static long tickFrequency = 50;
    @Comment("""
            What the table prefix of the database should be.
            This is useful, if you want to connect multiple servers to the same database
            but with separate currency systems.
            """)
    public static String tablePrefix = "";
    @Comment("""
            The pool size of the asynchronous executor
            we use to process some things asynchronously,
            like database queries.
            """)
    public static int asyncProcessorPoolSize = 1;
    @Comment("""
            What language file should we load from the lang folder?
            You can create your own aswell! We would appreciate if you
            contributed to the plugin by creating a pull request with your translation!
            """)
    public static String language = "en_US";
    @Comment("""
            If we should send debug messages in the console
            You shouldn't enable this, unless you want to see what happens in the code.
            """)
    public static boolean debug = false;
    @Comment("Do not touch!")
    public static int configVersion = 1;
    private YamlConfiguration<?> config = null;

    public static boolean reload() {
        return INSTANCE.refreshConfig();
    }

    private boolean refreshConfig() {
        Path path = FileUtils.PLUGIN_DIRECTORY.resolve("config.yml");
        if (Files.exists(path)) {
            if (!YamlUtils.suggest(path.toFile())) {
                return false;
            }
        }

        if (this.config == null) {
            this.config = YamlConfiguration.of(path, Config.class)
                    .configVersion(1, "config-version")
                    .withDumperOptions(options -> {
                        options.setPrettyFlow(true);
                        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                    }).build();
        }

        this.config.load();
        return true;
    }
}
