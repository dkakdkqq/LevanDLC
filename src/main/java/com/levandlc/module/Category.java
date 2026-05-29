package com.levandlc.module;

/**
 * Logical grouping used to organize {@link Module}s, e.g. in a UI or click GUI.
 */
public enum Category {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    RENDER("Render");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return the human-readable name shown to the player.
     */
    public String getDisplayName() {
        return displayName;
    }
}
