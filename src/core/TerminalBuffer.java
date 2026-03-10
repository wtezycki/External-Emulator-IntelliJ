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
    private int cursorX;
    private int cursorY;

    // Attributes:
    private CellAttribute currAttribute;

    public TerminalBuffer(int width, int height, int maxScrollback) {
        this.width = width;
        this.height = height;
        screen = new ArrayList<>(height);

        this.maxScrollback = maxScrollback;
        this.scrollback = new ArrayDeque<>();

        this.cursorX = 0;
        this.cursorY = 0;

        this.currAttribute = new CellAttribute();

        for (int i = 0; i < height; i++) { screen.add(new TerminalLine(width)); }
    }

    // Cursor methods:
    public void setCursorPosition(int x, int y) {
        // Check boundaries
        // Method 1: Block invalid position
//        if ((x < 0 || x >= width) ||
//                (y < 0 || y >= height) ) {
//            return;
//        }
//        this.cursorX = x;
//        this.cursorY = y;

        // Method 2 (better): Allow user to go to the most valid cell
        this.cursorX = Math.max(0, Math.min(x, width - 1));
        this.cursorY = Math.max(0, Math.min(y, height - 1));
    }

    public void moveCursor(int dx, int dy) {
        setCursorPosition(cursorX + dx,
                        cursorY + dy);
    }

}
