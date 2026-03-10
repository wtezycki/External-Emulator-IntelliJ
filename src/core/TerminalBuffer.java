package core;

import models.CellAttribute;
import models.TerminalLine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

// Supports:
// - Setup (width | height | max scrollback)
// - Set current attributes
// - Cursor (set position (row, col) | move cursor (and stay within border of terminal))
public class TerminalBuffer {

    // Screen:
    private final int width;
    private final int height;
    private final List<TerminalLine> screen;

    // Scrollback:
    private final int maxScrollback;
    // Deque is perfect data structure for scrollback, because it allows newly added operations to push to deque,
    // and simultaneously pop the latest one.
    private final Deque<TerminalLine> scrollback;

    // Cursor:
    private int cursor_x;
    private int cursor_y;

    // Attributes:
    private CellAttribute currAttribute;

    public TerminalBuffer(int width, int height, int maxScrollback) {
        this.width = width;
        this.height = height;
        screen = new ArrayList<TerminalLine>(height);

        this.maxScrollback = maxScrollback;
        this.scrollback = new ArrayDeque<TerminalLine>();

        this.cursor_x = 0;
        this.cursor_y = 0;

        this.currAttribute = new CellAttribute();

        for (int i = 0; i < height; i++) { screen.add(new TerminalLine(width)); }
    }



}
