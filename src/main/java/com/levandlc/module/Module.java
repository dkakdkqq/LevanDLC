package com.levandlc.module;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import org.lwjgl.glfw.GLFW;

/**
 * Base class for every feature in the mod.
 *
 * <p>A module owns a {@link #name}, a {@link Category} and an optional toggle
 * {@link #key keybind}. Lifecycle hooks are invoked by the {@link ModuleManager}:
 * <ul>
 *     <li>{@link #onEnable()} / {@link #onDisable()} on state transitions,</li>
 *     <li>{@link #onUpdate()} once per client tick while enabled,</li>
 *     <li>{@link #onRender3D(WorldRenderContext)} once per frame while enabled.</li>
 * </ul>
 * Subclasses override only the hooks they care about.
 */
public abstract class Module {

    private final String name;
    private final Category category;

    /** GLFW key code used to toggle the module, or {@link GLFW#GLFW_KEY_UNKNOWN} if unbound. */
    private int key;

    private boolean enabled;

    protected Module(String name, Category category, int key) {
        this.name = name;
        this.category = category;
        this.key = key;
        this.enabled = false;
    }

    /**
     * Creates an unbound module (no toggle keybind).
     */
    protected Module(String name, Category category) {
        this(name, category, GLFW.GLFW_KEY_UNKNOWN);
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

    /**
     * Called every frame during world rendering while the module is enabled.
     *
     * @param context the Fabric world render context (matrices, camera, tick delta...).
     */
    public void onRender3D(WorldRenderContext context) {
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

    public Category getCategory() {
        return category;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }
}
