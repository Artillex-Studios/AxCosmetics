package com.artillexstudios.axcosmetics.config;

import com.artillexstudios.axapi.config.YamlConfiguration;
import com.artillexstudios.axapi.config.annotation.Comment;
import com.artillexstudios.axapi.config.annotation.ConfigurationPart;
import com.artillexstudios.axapi.config.annotation.Ignored;
import com.artillexstudios.axapi.config.annotation.Serializable;
import com.artillexstudios.axapi.libs.snakeyaml.DumperOptions;
import com.artillexstudios.axapi.utils.YamlUtils;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcosmetics.AxCosmeticsPlugin;
import com.artillexstudios.axcosmetics.utils.FileUtils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Language implements ConfigurationPart {
    private static final Path LANGUAGE_DIRECTORY = FileUtils.PLUGIN_DIRECTORY.resolve("language");
    private static final Language INSTANCE = new Language();
    public static String prefix = "AxCosmetics";

    public static Reload reload = new Reload();

    @Serializable
    public static class Reload {
        public String success = "<#00FF00>Successfully reloaded the configurations of the plugin in <white><time></white>ms!";
        public String fail = "<#FF0000>There were some issues while reloading file(s): <white><files></white>! Please check out the console for more information! <br>Reload done in: <white><time></white>ms!";
    }

    public static String equip = "<#00FF00>You have successfully equipped <cosmetic>!";
    public static String unequip = "<#FF0000>You have successfully unequipped <cosmetic>!";
    public static String give = "<#00FF00>You have successfully given <cosmetic> <edition> to <player>!";
    public static String receive = "<#00FF00>You were given <cosmetic> <edition>!";
    public static String cosmeticNotFound = "<#FF0000>No cosmetic found with type <type>!";
    public static String adminGuiMessage = "<#FF0000>Shift+left click on a cosmetic to delete it!";

    @Comment("Do not touch!")
    public static int configVersion = 1;
    @Ignored
    public static String lastLanguage;
    private YamlConfiguration<?> config = null;

    public static boolean reload() {
        if (Config.debug) {
            LogUtils.debug("Reload called on language!");
        }
        FileUtils.copyFromResource("language");

        return INSTANCE.refreshConfig();
    }

    private boolean refreshConfig() {
        if (Config.debug) {
            LogUtils.debug("Refreshing language");
        }
        Path path = LANGUAGE_DIRECTORY.resolve(Config.language + ".yml");
        boolean shouldDefault = false;
        if (Files.exists(path)) {
            if (Config.debug) {
                LogUtils.debug("File exists");
            }
            if (!YamlUtils.suggest(path.toFile())) {
                return false;
            }
        } else {
            shouldDefault = true;
            path = LANGUAGE_DIRECTORY.resolve("en_US.yml");
            LogUtils.error("No language configuration was found with the name {}! Defaulting to en_US...", Config.language);
        }

        // The user might have changed the config
        if (this.config == null || (lastLanguage != null && lastLanguage.equalsIgnoreCase(Config.language))) {
            lastLanguage = shouldDefault ? "en_US" : Config.language;
            if (Config.debug) {
                LogUtils.debug("Set lastLanguage to {}", lastLanguage);
            }
            InputStream defaults = AxCosmeticsPlugin.instance().getResource("language/" + lastLanguage + ".yml");
            if (defaults == null) {
                if (Config.debug) {
                    LogUtils.debug("Defaults are null, defaulting to en_US.yml");
                }
                defaults = AxCosmeticsPlugin.instance().getResource("language/en_US.yml");
            }

            if (Config.debug) {
                LogUtils.debug("Loading config from file {} with defaults {}", path, defaults);
            }

            this.config = YamlConfiguration.of(path, Language.class)
                    .configVersion(1, "config-version")
                    .withDefaults(defaults)
                    .withDumperOptions(options -> {
                        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                        options.setSplitLines(false);
                    }).build();
        }

        this.config.load();
        return true;
    }
}
