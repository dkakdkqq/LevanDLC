package com.levandlc;

import com.levandlc.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client entrypoint for the mod.
 *
 * <p>Wires the {@link ModuleManager} into Fabric's client lifecycle and world
 * render events. Concrete modules are registered in {@link #registerModules()}.
 */
public class LevanDLC implements ClientModInitializer {

    public static final String MOD_ID = "levandlc";
    public static final String MOD_NAME = "LevanDLC";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    private static final ModuleManager MODULE_MANAGER = new ModuleManager();

    @Override
    public void onInitializeClient() {
        LOGGER.info("[{}] Initializing client...", MOD_NAME);

        registerModules();
        registerEventHandlers();

        LOGGER.info("[{}] Loaded {} module(s).", MOD_NAME, MODULE_MANAGER.getModules().size());
    }

    /**
     * Registers the mod's modules. Add new features here, e.g.:
     * <pre>{@code MODULE_MANAGER.register(new SprintModule());}</pre>
     */
    private void registerModules() {
        // No modules yet - this is the framework skeleton.
    }

    /**
     * Binds the manager to Fabric's client tick and world render events.
     */
    private void registerEventHandlers() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.getWindow() != null) {
                MODULE_MANAGER.handleKeyPresses(client.getWindow().getHandle());
            }
            // Only tick gameplay logic while in-world.
            if (client.player != null && client.world != null) {
                MODULE_MANAGER.onUpdate();
            }
        });

        WorldRenderEvents.AFTER_TRANSLUCENT.register(MODULE_MANAGER::onRender3D);
    }

    public static ModuleManager getModuleManager() {
        return MODULE_MANAGER;
    }
}
