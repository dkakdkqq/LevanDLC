package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static com.levandlc.module.Category.*;

/**
 * The mod's module catalogue. {@link #all()} builds the full list of features
 * shown in the ClickGUI, grouped by {@link Category}.
 *
 * <p>Each entry is a {@link SimpleModule}: it toggles, binds to a key, and shows
 * in the GUI with a description. Gameplay behaviour is intentionally left to be
 * implemented per-module on top of a confirmed Minecraft render/input API.
 */
public final class Modules {

    private Modules() {
    }

    private static final int NONE = GLFW.GLFW_KEY_UNKNOWN;

    public static List<Module> all() {
        List<Module> m = new ArrayList<>();

        // ---------------- COMBAT ----------------
        m.add(new SimpleModule("KillAura", "Automatically attacks nearby entities", COMBAT, GLFW.GLFW_KEY_R));
        m.add(new SimpleModule("Criticals", "Always deal critical hits", COMBAT));
        m.add(new SimpleModule("AutoCrystal", "Places and breaks end crystals", COMBAT));
        m.add(new SimpleModule("AutoTotem", "Keeps a totem in your off-hand", COMBAT));
        m.add(new SimpleModule("Aimbot", "Smoothly aims at the closest target", COMBAT));
        m.add(new SimpleModule("Reach", "Extends your attack range", COMBAT));
        m.add(new SimpleModule("AutoAnchor", "Automates respawn anchor combat", COMBAT));
        m.add(new SimpleModule("Hitboxes", "Expands entity hitboxes", COMBAT));
        m.add(new SimpleModule("TriggerBot", "Attacks when crosshair is on target", COMBAT));
        m.add(new SimpleModule("AntiBot", "Ignores fake/bot players", COMBAT));
        m.add(new SimpleModule("SuperKnockback", "Increases knockback dealt", COMBAT));
        m.add(new SimpleModule("AutoArmor", "Equips the best armor automatically", COMBAT));
        m.add(new SimpleModule("Velocity", "Reduces or cancels knockback taken", COMBAT));
        m.add(new SimpleModule("BowAimbot", "Auto-aims projectiles at targets", COMBAT));

        // ---------------- MOVEMENT ----------------
        m.add(new SimpleModule("Sprint", "Always sprint forward", MOVEMENT));
        m.add(new SimpleModule("Flight", "Lets you fly freely", MOVEMENT, GLFW.GLFW_KEY_G));
        m.add(new SimpleModule("Speed", "Move faster than normal", MOVEMENT));
        m.add(new SimpleModule("NoFall", "Prevents fall damage", MOVEMENT));
        m.add(new SimpleModule("Step", "Walk up full blocks instantly", MOVEMENT));
        m.add(new SimpleModule("Spider", "Climb walls like a spider", MOVEMENT));
        m.add(new SimpleModule("Jesus", "Walk on water surfaces", MOVEMENT));
        m.add(new SimpleModule("AutoWalk", "Walks forward automatically", MOVEMENT));
        m.add(new SimpleModule("Strafe", "Air-strafe for smoother control", MOVEMENT));
        m.add(new SimpleModule("ElytraFly", "Enhanced elytra flight control", MOVEMENT));
        m.add(new SimpleModule("LongJump", "Jump much further than normal", MOVEMENT));
        m.add(new SimpleModule("Velocity+", "Fine-grained knockback control", MOVEMENT));
        m.add(new SimpleModule("NoSlow", "Removes slowdown from items/blocks", MOVEMENT));
        m.add(new SimpleModule("Sneak", "Permanently sneak", MOVEMENT));

        // ---------------- RENDER ----------------
        m.add(new SimpleModule("ESP", "Highlights entities through walls", RENDER));
        m.add(new SimpleModule("Tracers", "Draws lines to entities", RENDER));
        m.add(new SimpleModule("Fullbright", "Maximum brightness everywhere", RENDER, GLFW.GLFW_KEY_H));
        m.add(new SimpleModule("Nametags", "Enlarged readable nametags", RENDER));
        m.add(new SimpleModule("StorageESP", "Highlights chests and storage", RENDER));
        m.add(new SimpleModule("Xray", "See through blocks to find ores", RENDER, GLFW.GLFW_KEY_X));
        m.add(new SimpleModule("Trajectories", "Predicts projectile paths", RENDER));
        m.add(new SimpleModule("NoRender", "Disables annoying render effects", RENDER));
        m.add(new SimpleModule("CameraClip", "Camera passes through blocks", RENDER));
        m.add(new SimpleModule("ViewModel", "Customize held-item rendering", RENDER));
        m.add(new SimpleModule("Chams", "Renders entities with a shader", RENDER));
        m.add(new SimpleModule("BlockESP", "Highlights configured blocks", RENDER));
        m.add(new SimpleModule("Breadcrumbs", "Leaves a trail where you walk", RENDER));
        m.add(new SimpleModule("Zoom", "Optifine-style zoom", RENDER, GLFW.GLFW_KEY_C));

        // ---------------- PLAYER ----------------
        m.add(new SimpleModule("AutoTool", "Switches to the best tool", PLAYER));
        m.add(new SimpleModule("FastPlace", "Removes block placement delay", PLAYER));
        m.add(new SimpleModule("FastBreak", "Breaks blocks faster", PLAYER));
        m.add(new SimpleModule("AutoEat", "Eats food when hungry", PLAYER));
        m.add(new SimpleModule("Scaffold", "Auto-bridges below you", PLAYER));
        m.add(new SimpleModule("ChestStealer", "Empties containers instantly", PLAYER));
        m.add(new SimpleModule("AutoFish", "Automatic fishing", PLAYER));
        m.add(new SimpleModule("FreeCam", "Detaches the camera from you", PLAYER, GLFW.GLFW_KEY_F4));
        m.add(new SimpleModule("InvManager", "Sorts and manages inventory", PLAYER));
        m.add(new SimpleModule("NoInteract", "Prevents accidental interactions", PLAYER));
        m.add(new SimpleModule("Blink", "Holds back packets temporarily", PLAYER));

        // ---------------- UTIL ----------------
        m.add(new SimpleModule("HUD", "On-screen information display", UTIL));
        m.add(new SimpleModule("FPSDisplay", "Shows current frame rate", UTIL));
        m.add(new SimpleModule("CoordsDisplay", "Shows your coordinates", UTIL));
        m.add(new SimpleModule("KeyStrokes", "On-screen keystroke overlay", UTIL));
        m.add(new SimpleModule("ArmorHUD", "Displays armor durability", UTIL));
        m.add(new SimpleModule("PingDisplay", "Shows your latency", UTIL));
        m.add(new SimpleModule("ClickTimer", "CPS counter overlay", UTIL));
        m.add(new SimpleModule("AutoReconnect", "Reconnects after disconnects", UTIL));
        m.add(new SimpleModule("DiscordRPC", "Rich presence integration", UTIL));
        m.add(new SimpleModule("NameProtect", "Hides your name in screenshots", UTIL));
        m.add(new SimpleModule("ServerCrasher", "Stress-test tool (disabled)", UTIL));
        m.add(new SimpleModule("Notifications", "Toast notifications for events", UTIL));

        return m;
    }
}
