package com.artillexstudios.axcosmetics.gui;

import com.artillexstudios.axapi.config.YamlConfiguration;
import com.artillexstudios.axapi.utils.Cooldown;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axapi.utils.NumberUtils;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcosmetics.api.user.User;
import com.artillexstudios.axcosmetics.gui.actions.Action;
import com.artillexstudios.axcosmetics.gui.actions.Actions;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class GuiBase {
    protected static final Cooldown<UUID> clickCooldown = new Cooldown<>();
    private final User user;
    private final YamlConfiguration<?> config;
    private final BaseGui gui;

    public GuiBase(User user, YamlConfiguration<?> config, boolean paginated, boolean latePopulate) {
        this.user = user;
        this.config = config;
        if (paginated) {
            this.gui = Gui.paginated()
                    .title(Component.empty())
                    .pageSize(config.getInteger("page-size"))
                    .rows(config.getInteger("rows"))
                    .disableAllInteractions()
                    .create();
        } else {
            this.gui = Gui.gui()
                    .title(Component.empty())
                    .rows(config.getInteger("rows"))
                    .disableAllInteractions()
                    .create();
        }

        if (!latePopulate) {
            this.populate();
        }
    }

    public GuiBase(User user, YamlConfiguration<?> config, boolean paginated) {
        this(user, config, paginated, false);
    }

    public void populate() {
        this.gui.updateTitle(StringUtils.format(this.config.getString("title")));
        for (Map<Object, Object> item : (List<Map<Object, Object>>) this.config.getList("items")) {
            Object slotConfig = item.get("slots");
            if (slotConfig == null) {
                LogUtils.warn("Could not load item {} as it does not have any slots set!", item);
                continue;
            }

            IntList slots = this.slots(slotConfig);


            List<String> actionSet = (List<String>) item.get("actions");
            List<Action<?>> actions = Actions.compile(actionSet);
            List<Object> values = Actions.parseAll(actionSet);

            this.gui.setItem(slots, new GuiItem(new ItemBuilder(item).get(), event -> {
                UUID uuid = event.getWhoClicked().getUniqueId();
                if (clickCooldown.hasCooldown(uuid)) {
                    return;
                }

                clickCooldown.addCooldown(uuid, 250);
                Actions.execute(this.user, this, actions, values);
            }));
        }
    }

    public IntArrayList slots(Object slots) {
        if (slots instanceof Integer integer) {
            return new IntArrayList(List.of(integer));
        } else if (slots instanceof List<?> list) {
            IntArrayList integers = new IntArrayList();
            List<Object> l = (List<Object>) list;
            for (Object obj : l) {
                if (obj instanceof Integer integer) {
                    integers.add(integer);
                } else if (obj instanceof String str) {
                    if (NumberUtils.isInt(str)) {
                        integers.add(Integer.parseInt(str));
                        continue;
                    }

                    String[] split = str.split("-");
                    int min = Integer.parseInt(split[0]);
                    int max = Integer.parseInt(split[1]);
                    for (int i = min; i <= max; i++) {
                        integers.add(i);
                    }
                }
            }

            return integers;
        }

        return new IntArrayList();
    }

    public YamlConfiguration<?> config() {
        return this.config;
    }

    public BaseGui gui() {
        return this.gui;
    }

    public abstract void open();

    public void open(int page) {
        this.open();
    }

    public User user() {
        return this.user;
    }
}
