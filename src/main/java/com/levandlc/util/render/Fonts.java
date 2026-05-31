package com.levandlc.util.render;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Custom-font support for the GUI, Celestial-style.
 *
 * <p>Minecraft resolves fonts by an {@link Identifier} that points at a provider
 * JSON in {@code assets/<namespace>/font/<path>.json}. We ship
 * {@code assets/levandlc/font/regular.json}; to use a real TTF, drop the file in
 * {@code assets/levandlc/font/} and change {@code regular.json} to a
 * {@code "type": "ttf"} provider (see the README note).
 *
 * <p>Text is rendered by attaching this font to a {@link Style} and drawing a
 * styled {@link Text}, so it works through the normal text renderer on any
 * 1.21.x build ({@code Identifier.of} + {@code Style.withFont} are stable here).
 */
public final class Fonts {

    private Fonts() {
    }

    /** Our custom GUI font id -> assets/levandlc/font/regular.json. */
    public static final Identifier REGULAR = Identifier.of("levandlc", "regular");

    private static final Style REGULAR_STYLE = Style.EMPTY.withFont(REGULAR);

    /** Wraps a string in a {@link Text} that uses our custom font. */
    public static Text styled(String text) {
        MutableText t = Text.literal(text);
        return t.setStyle(REGULAR_STYLE);
    }

    public static Style style() {
        return REGULAR_STYLE;
    }
}
