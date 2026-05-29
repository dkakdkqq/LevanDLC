package com.levandlc.module;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * Central registry that owns every {@link Module} and dispatches engine events
 * (keybinds, ticks and world rendering) to them.
 */
public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    /**
     * Tracks modules whose keybind is currently held down so that toggling only
     * fires once per key press (edge detection) instead of every tick.
     */
    private final Set<Module> heldKeys = Collections.newSetFromMap(new IdentityHashMap<>());

    // ------------------------------------------------------------------
    // Registration.
    // ------------------------------------------------------------------

    public void register(Module module) {
        modules.add(module);
    }

    public void registerAll(Module... mods) {
        for (Module module : mods) {
            register(module);
        }
    }

    // ------------------------------------------------------------------
    // Event dispatch - invoked from the mod's event handlers.
    // ------------------------------------------------------------------

    /**
     * Handles keybind edge-detection and toggles modules accordingly.
     * Call once per client tick.
     *
     * @param window the GLFW window handle
     *               ({@code MinecraftClient.getInstance().getWindow().getHandle()}).
     */
    public void handleKeyPresses(long window) {
        for (Module module : modules) {
            int key = module.getKey();
            if (key == GLFW.GLFW_KEY_UNKNOWN) {
                continue;
            }

            boolean down = InputUtil.isKeyPressed(window, key);
            if (down) {
                // Set#add returns true only on the first frame the key is held.
                if (heldKeys.add(module)) {
                    module.toggle();
                }
            } else {
                heldKeys.remove(module);
            }
        }
    }

    /** Dispatches the per-tick update to every enabled module. */
    public void onUpdate() {
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onUpdate();
            }
        }
    }

    /** Dispatches the 3D render pass to every enabled module. */
    public void onRender3D(WorldRenderContext context) {
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onRender3D(context);
            }
        }
    }

    // ------------------------------------------------------------------
    // Queries.
    // ------------------------------------------------------------------

    /** @return an unmodifiable view of all registered modules. */
    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }

    /** @return all modules belonging to the given {@link Category}. */
    public List<Module> getModules(Category category) {
        List<Module> result = new ArrayList<>();
        for (Module module : modules) {
            if (module.getCategory() == category) {
                result.add(module);
            }
        }
        return result;
    }

    /** @return the module with the given name (case-insensitive), or {@code null}. */
    public Module getModule(String name) {
        for (Module module : modules) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }
}
