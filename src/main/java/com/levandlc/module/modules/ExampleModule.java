package com.levandlc.module.modules;

import com.levandlc.LevanDLC;
import com.levandlc.module.Category;
import com.levandlc.module.Module;

/**
 * A no-op placeholder module used to populate the ClickGUI in this skeleton.
 *
 * <p>Replace these with real features - it only logs its lifecycle so you can see
 * the toggle/keybind wiring working end to end.
 */
public class ExampleModule extends Module {

    public ExampleModule(String name, Category category, int key) {
        super(name, category, key);
    }

    public ExampleModule(String name, Category category) {
        super(name, category);
    }

    @Override
    public void onEnable() {
        LevanDLC.LOGGER.info("[Module] {} enabled", getName());
    }

    @Override
    public void onDisable() {
        LevanDLC.LOGGER.info("[Module] {} disabled", getName());
    }
}
