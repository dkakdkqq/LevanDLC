package com.levandlc;

import com.levandlc.gui.clickgui.ClickGuiScreen;
import com.levandlc.module.ModuleManager;
import com.levandlc.module.modules.Modules;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client entrypoint for the mod.
 *
 * <p>Wires the {@link ModuleManager} into Fabric's client tick lifecycle and
 * toggles the {@link ClickGuiScreen} on a keybind.
 */
public class LevanDLC implements ClientModInitializer {

    public static final String MOD_ID = "levandlc";
    public static final String MOD_NAME = "LevanDLC";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    /** Key that opens / closes the ClickGUI. */
    public static final int CLICK_GUI_KEY = GLFW.GLFW_KEY_RIGHT_SHIFT;

    private static final ModuleManager MODULE_MANAGER = new ModuleManager();

    /** Edge-detection state for the ClickGUI toggle key. */
    private boolean guiKeyHeld = false;

    @Override
    public void onInitializeClient() {
        LOGGER.info("[{}] Initializing client...", MOD_NAME);

        registerModules();
        registerEventHandlers();

        LOGGER.info("[{}] Loaded {} module(s). Press RIGHT SHIFT to open the ClickGUI.",
                MOD_NAME, MODULE_MANAGER.getModules().size());
    }

    /**
     * Registers the mod's module catalogue (see {@link Modules#all()}).
     */
    private void registerModules() {
        for (var module : Modules.all()) {
            MODULE_MANAGER.register(module);
        }
    }

    /**
     * Binds the manager to Fabric's client tick event.
     */
    private void registerEventHandlers() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            handleClickGuiKey(client);

            // Module keybinds only fire when no screen is capturing input.
            if (client.currentScreen == null && client.getWindow() != null) {
                MODULE_MANAGER.handleKeyPresses(client.getWindow().getHandle());
            }

            // Only tick gameplay logic while in-world.
            if (client.player != null && client.world != null) {
                MODULE_MANAGER.onUpdate();
            }
        });
    }

    /** Toggles the ClickGUI on a single press of {@link #CLICK_GUI_KEY} (edge-detected). */
    private void handleClickGuiKey(MinecraftClient client) {
        if (client.getWindow() == null) {
            return;
        }
        boolean down = GLFW.glfwGetKey(client.getWindow().getHandle(), CLICK_GUI_KEY) == GLFW.GLFW_PRESS;
        if (down && !guiKeyHeld) {
            if (client.currentScreen == null) {
                client.setScreen(new ClickGuiScreen());
            } else if (client.currentScreen instanceof ClickGuiScreen) {
                client.setScreen(null);
            }
        }
        guiKeyHeld = down;
    }

    public static ModuleManager getModuleManager() {
        return MODULE_MANAGER;
    }
}
