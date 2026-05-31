package com.levandlc.util.render;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Custom-font support for the GUI.
 *
 * <p>Minecraft resolves fonts by an {@link Identifier} pointing at a provider
 * JSON in {@code assets/<namespace>/font/<path>.json}. We ship
 * {@code assets/levandlc/font/regular.json}.
 *
 * <p><b>Why the font is not force-applied via {@code Style.withFont} here:</b>
 * the {@code Style.withFont(...)} parameter type changed in the 1.21.11 yarn
 * mappings (it no longer accepts a raw {@link Identifier}), which broke the
 * build. To keep the project compiling on every machine, {@link #styled(String)}
 * currently returns plain text using the default font.
 *
 * <p><b>To enable the custom font</b> (one line), once you confirm the exact
 * signature from your IDE autocomplete on {@code Style.EMPTY.withFont(...)},
 * build a {@code Style} with it and apply it in {@link #styled(String)} via
 * {@code Text.literal(text).setStyle(style)}.
 */
public final class Fonts {

    private Fonts() {
    }

    /** Our custom GUI font id -> assets/levandlc/font/regular.json. */
    public static final Identifier REGULAR = Identifier.of("levandlc", "regular");

    /**
     * Wraps a string in a {@link Text} for the renderer. Returns plain text using
     * the default font for now (see class docs for enabling the custom font).
     */
    public static Text styled(String text) {
        return Text.literal(text);
    }
}
