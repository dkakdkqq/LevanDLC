package com.levandlc.gui.clickgui;

import com.levandlc.util.render.ColorUtil;

/**
 * Central color/metrics palette for the ClickGUI - a dark panel with a purple
 * accent, inspired by the reference screenshot.
 *
 * <p>Tweak everything about the GUI's look from this one place.
 */
public final class Theme {

    private Theme() {
    }

    // ---- Colors (0xAARRGGBB) ----
    public static final int BACKDROP        = ColorUtil.rgba(0, 0, 0, 150);   // world dim behind GUI
    public static final int WINDOW_BG       = ColorUtil.rgb(18, 16, 24);      // main window
    public static final int SIDEBAR_BG      = ColorUtil.rgb(24, 21, 32);      // category column
    public static final int CONTENT_BG      = ColorUtil.rgb(20, 18, 28);      // module grid area
    public static final int CARD_BG         = ColorUtil.rgb(31, 27, 42);      // module card (off)
    public static final int CARD_BG_HOVER   = ColorUtil.rgb(40, 34, 54);      // module card hovered
    public static final int SEARCH_BG       = ColorUtil.rgb(31, 27, 42);

    public static final int ACCENT          = ColorUtil.rgb(178, 102, 255);   // primary purple
    public static final int ACCENT_DARK     = ColorUtil.rgb(126, 66, 196);
    public static final int ACCENT_SOFT     = ColorUtil.rgba(178, 102, 255, 60);

    public static final int TEXT            = ColorUtil.rgb(235, 232, 242);
    public static final int TEXT_DIM        = ColorUtil.rgb(150, 144, 165);
    public static final int TEXT_MUTED      = ColorUtil.rgb(108, 102, 122);

    public static final int DIVIDER         = ColorUtil.rgb(38, 34, 50);
    public static final int SHADOW          = ColorUtil.rgba(0, 0, 0, 180);

    // ---- Metrics ----
    public static final int WINDOW_W   = 440;
    public static final int WINDOW_H   = 280;
    public static final int SIDEBAR_W  = 120;
    public static final int HEADER_H   = 34;
    public static final int SEARCH_H   = 24;
    public static final int CARD_H     = 30;
    public static final int CARD_GAP   = 6;
    public static final int PADDING    = 10;
    public static final int CORNER     = 7;

    /** Accent gradient pair used for the title and toggle pills. */
    public static int accentGradientStart() {
        return ColorUtil.rgb(198, 130, 255);
    }

    public static int accentGradientEnd() {
        return ColorUtil.rgb(126, 66, 196);
    }
}
