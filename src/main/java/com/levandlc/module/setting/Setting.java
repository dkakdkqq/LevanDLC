package com.levandlc.module.setting;

/**
 * Base class for a module setting - a single named, user-adjustable value shown
 * in the ClickGUI's settings panel.
 */
public abstract class Setting {

    private final String name;

    protected Setting(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
