package com.levandlc.module.modules;

import com.levandlc.LevanDLC;
import com.levandlc.module.Category;
import com.levandlc.module.Module;

/**
 * A lightweight, fully-functional toggleable module with a name, description and
 * optional keybind. It logs its lifecycle so the toggle wiring is observable.
 *
 * <p>It deliberately carries no gameplay logic - that depends on version-specific
 * Minecraft APIs. Subclass it (or add {@code onUpdate} logic) to make a feature
 * actually do something; the GUI, toggling, keybinds and persistence all already
 * work through this class.
 */
public class SimpleModule extends Module {

    public SimpleModule(String name, String description, Category category, int key) {
        super(name, description, category, key);
    }

    public SimpleModule(String name, String description, Category category) {
        super(name, description, category);
    }

    /** Public passthrough so the registry can attach settings fluently. */
    public <T extends com.levandlc.module.setting.Setting> T with(T setting) {
        return addSetting(setting);
    }

    @Override
    public void onEnable() {
        LevanDLC.LOGGER.info("[Module] {} -> ON", getName());
    }

    @Override
    public void onDisable() {
        LevanDLC.LOGGER.info("[Module] {} -> OFF", getName());
    }
}
