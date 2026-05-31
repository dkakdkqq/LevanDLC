package com.levandlc.util.render;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.lang.reflect.Method;

/**
 * Custom-font support for the GUI.
 *
 * <p>Minecraft resolves fonts by an {@link Identifier} pointing at a provider
 * JSON in {@code assets/<namespace>/font/<path>.json}. We ship
 * {@code assets/levandlc/font/regular.json}.
 *
 * <p><b>Why reflection:</b> the parameter type of {@code Style.withFont(...)}
 * changed across the 1.21.x line - on 1.21.11 it no longer accepts a raw
 * {@link Identifier}, which broke a direct call at compile time. To stay
 * buildable on every toolchain we resolve a compatible {@code withFont} overload
 * reflectively <em>once</em> at class-load and cache the resulting {@link Style}.
 * If no compatible overload is found, we fall back to the default font - text
 * still renders, nothing crashes.
 */
public final class Fonts {

    private Fonts() {
    }

    /** Our custom GUI font id -> assets/levandlc/font/regular.json. */
    public static final Identifier REGULAR = Identifier.of("levandlc", "regular");

    /** Resolved once: a Style carrying our font, or Style.EMPTY if unavailable. */
    private static final Style STYLE = resolveStyle();

    private static Style resolveStyle() {
        try {
            for (Method m : Style.class.getMethods()) {
                if (!m.getName().equals("withFont") || m.getParameterCount() != 1) {
                    continue;
                }
                Class<?> param = m.getParameterTypes()[0];
                // Direct Identifier overload (older mappings).
                if (param.isAssignableFrom(Identifier.class)) {
                    return (Style) m.invoke(Style.EMPTY, REGULAR);
                }
                // Wrapper overload (e.g. a RegistryEntry/holder): try a single-arg
                // factory or constructor that takes an Identifier.
                Object wrapped = tryWrap(param, REGULAR);
                if (wrapped != null) {
                    return (Style) m.invoke(Style.EMPTY, wrapped);
                }
            }
        } catch (Throwable ignored) {
            // Any reflection/linkage issue -> default font.
        }
        return Style.EMPTY;
    }

    /** Attempts to wrap an Identifier into {@code target} via a 1-arg factory/ctor. */
    private static Object tryWrap(Class<?> target, Identifier id) {
        // Static factory methods taking an Identifier (e.g. of/create/method_*).
        for (Method m : target.getMethods()) {
            if (java.lang.reflect.Modifier.isStatic(m.getModifiers())
                    && m.getParameterCount() == 1
                    && m.getParameterTypes()[0].isAssignableFrom(Identifier.class)
                    && target.isAssignableFrom(m.getReturnType())) {
                try {
                    return m.invoke(null, id);
                } catch (Throwable ignored) {
                    // try next
                }
            }
        }
        return null;
    }

    /** Wraps a string in a {@link Text} that uses our custom font (or default). */
    public static Text styled(String text) {
        MutableText t = Text.literal(text);
        return t.setStyle(STYLE);
    }

    /** True if the custom font was successfully resolved and is being applied. */
    public static boolean isCustomFontActive() {
        return STYLE != Style.EMPTY;
    }
}
