package com.levandlc.module.setting;

import java.util.List;

/** A multiple-choice setting that cycles through a fixed list of string modes. */
public class ModeSetting extends Setting {

    private final List<String> modes;
    private int index;

    public ModeSetting(String name, String... modes) {
        super(name);
        this.modes = List.of(modes);
        this.index = 0;
    }

    public String getValue() {
        return modes.get(index);
    }

    public List<String> getModes() {
        return modes;
    }

    /** Advances to the next mode (wraps around). */
    public void cycle() {
        index = (index + 1) % modes.size();
    }

    /** Goes to the previous mode (wraps around). */
    public void cycleBack() {
        index = (index - 1 + modes.size()) % modes.size();
    }

    public void setValue(String mode) {
        int i = modes.indexOf(mode);
        if (i >= 0) {
            index = i;
        }
    }
}
