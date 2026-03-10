package core;

import models.Cell;
import models.CellAttribute;
import models.TerminalLine;
import models.enums.Color;

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
        setCursorPosition(cursorX + dx, cursorY + dy);
    }

    // EDITING TEXT:

    // Scrolling helper
    private void scroll() {
        if (cursorY >= height) {
            TerminalLine row = screen.removeFirst();
            // if scrollback history is full, remove the oldest line from screen
            if (scrollback.size() >= this.maxScrollback) {
                scrollback.pollFirst();
            }
            scrollback.addLast(row);
            screen.add(new TerminalLine(width));
        }
    }

    // Writing
    public void write(String text) {
        for (char ch : text.toCharArray()) {
            // Case '\n':
            if (ch == '\n') {
                cursorY++;
            }
            // Case '\r':
            else if (ch == '\r') {
                cursorX = 0;
            }
            // Normal writing
            else {
                TerminalLine currentLine = screen.get(cursorY); // y -> index of line

                Cell cell = new Cell(
                        ch,
                        currAttribute.getBgColor(),
                        currAttribute.getFgColor(),
                        currAttribute.getStyle()
                );

                currentLine.setCell(cursorX, cell);
                cursorX++;

                // Check wrapping
                if (cursorX >= width) {
                    cursorX = 0;
                    cursorY++;
                }

                // Scrolling
                scroll();
                cursorY = height - 1;
            }
        }
    }

    // Insert
    // I assumed that the cell which was popped is carried to the next line.
    public void insert(String text) {
        for (char ch : text.toCharArray()) {
            if (ch == '\n') {
                cursorY++;
                continue;           // continue, because this character is invisible
            } else if (ch == '\r') {
                cursorX = 0;
                continue;
            }

            Cell toInsert = new Cell(
                    ch,
                    currAttribute.getBgColor(),
                    currAttribute.getFgColor(),
                    currAttribute.getStyle()
            );

            int tempX = cursorX;
            int tempY = cursorY;

            while(true) {
                TerminalLine currLine = screen.get(tempY);
                Cell toCarry = currLine.insertCell(tempX, toInsert);

                if (Cell.checkIfEmpty(toCarry)) {
                    break;
                }

                toInsert = toCarry;
                tempY++;
                tempX=0;

                if (tempY >= height) {
                    scroll();
                    tempY = height - 1;
                }
            }

            cursorX++;  // place for next character

            // Check boundaries
            if (cursorX >= width) {
                cursorX = 0;
                cursorY++;
            }
            if (cursorY >= height) {
                scroll();
                cursorY = height - 1;
            }
        }
    }

    // Fill line
    public void fillLine(char ch) {
        TerminalLine currentLine = screen.get(cursorY);

        Cell cell = new Cell(
                ch,
                currAttribute.getBgColor(),
                currAttribute.getFgColor(),
                currAttribute.getStyle()
        );
        currentLine.fill(cell);
    }

    // Cleaning screen
    public void clearScreen() {
        for (int i = 0; i < height; i++) {
            TerminalLine row = screen.get(i);
            for(int j = 0; j < width; j++) {
                row.setCell(j, Cell.createEmpty());
            }
        }
        setCursorPosition(0, 0);
    }

    public void clearScreenAndScrollback() {
        clearScreen();
        scrollback.clear();
    }



}
