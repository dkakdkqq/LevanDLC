package com.levandlc.module;

import com.levandlc.module.setting.Setting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for every feature in the mod.
 *
 * <p>A module owns a {@link #name}, a {@link #description}, a {@link Category}
 * and an optional toggle {@link #key keybind}. Lifecycle hooks are invoked by
 * the {@link ModuleManager}:
 * <ul>
 *     <li>{@link #onEnable()} / {@link #onDisable()} on state transitions,</li>
 *     <li>{@link #onUpdate()} once per client tick while enabled.</li>
 * </ul>
 * Subclasses override only the hooks they care about.
 *
 * <p>Note: a world-render hook is intentionally omitted here. World-space
 * rendering depends on Fabric/Minecraft render APIs that change frequently
 * between 1.21.x builds, so it is kept out of the core to keep this class
 * stable and compilable.
 */
public abstract class Module {

    private final String name;
    private final String description;
    private final Category category;

    /** Per-module configurable settings, shown in the GUI's settings panel. */
    private final List<Setting> settings = new ArrayList<>();

    /** GLFW key code used to toggle the module, or {@link GLFW#GLFW_KEY_UNKNOWN} if unbound. */
    private int key;

    private boolean enabled;

    protected Module(String name, String description, Category category, int key) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.key = key;
        this.enabled = false;
    }

    protected Module(String name, String description, Category category) {
        this(name, description, category, GLFW.GLFW_KEY_UNKNOWN);
    }

    protected Module(String name, Category category, int key) {
        this(name, "", category, key);
    }

    /** Creates an unbound module (no toggle keybind). */
    protected Module(String name, Category category) {
        this(name, "", category, GLFW.GLFW_KEY_UNKNOWN);
    }

    // ------------------------------------------------------------------
    // Lifecycle hooks - override as needed.
    // ------------------------------------------------------------------

    /** Called once when the module transitions from disabled to enabled. */
    public void onEnable() {
    }

    /** Called once when the module transitions from enabled to disabled. */
    public void onDisable() {
    }

    /** Called every client tick while the module is enabled. */
    public void onUpdate() {
    }

    // ------------------------------------------------------------------
    // State management.
    // ------------------------------------------------------------------

    /** Flips the enabled state, firing the appropriate lifecycle hook. */
    public void toggle() {
        setEnabled(!enabled);
    }

    /**
     * Sets the enabled state. Fires {@link #onEnable()} or {@link #onDisable()}
     * only when the state actually changes.
     */
    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }
        this.enabled = enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    // ------------------------------------------------------------------
    // Accessors.
    // ------------------------------------------------------------------

    public boolean isEnabled() {
        return enabled;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    // ------------------------------------------------------------------
    // Settings.
    // ------------------------------------------------------------------

    /** Registers a setting and returns it (for fluent field assignment). */
    protected <T extends Setting> T addSetting(T setting) {
        settings.add(setting);
        return setting;
    }

    public List<Setting> getSettings() {
        return settings;
    }

    public boolean hasSettings() {
        return !settings.isEmpty();
    }
}
