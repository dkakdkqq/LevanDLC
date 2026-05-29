package com.levandlc;

import com.levandlc.gui.clickgui.ClickGuiScreen;
import com.levandlc.module.Category;
import com.levandlc.module.ModuleManager;
import com.levandlc.module.modules.ExampleModule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client entrypoint for the mod.
 *
 * <p>Wires the {@link ModuleManager} into Fabric's client lifecycle and world
 * render events, and toggles the {@link ClickGuiScreen} on a keybind.
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
     * Registers the mod's modules. These are no-op {@link ExampleModule}s purely
     * to populate the ClickGUI - replace them with real features.
     */
    private void registerModules() {
        MODULE_MANAGER.registerAll(
                new ExampleModule("KillAura", Category.COMBAT, GLFW.GLFW_KEY_R),
                new ExampleModule("Criticals", Category.COMBAT),
                new ExampleModule("AutoCrystal", Category.COMBAT),

                new ExampleModule("Sprint", Category.MOVEMENT),
                new ExampleModule("Velocity", Category.MOVEMENT),
                new ExampleModule("Flight", Category.MOVEMENT, GLFW.GLFW_KEY_G),

                new ExampleModule("ESP", Category.RENDER),
                new ExampleModule("Tracers", Category.RENDER),
                new ExampleModule("Fullbright", Category.RENDER, GLFW.GLFW_KEY_H)
        );
    }

    /**
     * Binds the manager to Fabric's client tick and world render events.
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

        WorldRenderEvents.AFTER_TRANSLUCENT.register(MODULE_MANAGER::onRender3D);
    }

    /** Toggles the ClickGUI on a single press of {@link #CLICK_GUI_KEY} (edge-detected). */
    private void handleClickGuiKey(MinecraftClient client) {
        if (client.getWindow() == null) {
            return;
        }
        boolean down = InputUtil.isKeyPressed(client.getWindow().getHandle(), CLICK_GUI_KEY);
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
