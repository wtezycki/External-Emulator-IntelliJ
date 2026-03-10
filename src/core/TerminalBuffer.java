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
// I assumed that negative y coordinates mean that it is scrollback.
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

    public void insertEmptyLineAtBottom() {
        TerminalLine row = screen.removeFirst();
        // if scrollback history is full, remove the oldest line from screen
        if (scrollback.size() >= this.maxScrollback) {
            scrollback.pollFirst();
        }
        scrollback.addLast(row);
        screen.add(new TerminalLine(width));
    }

    // Scrolling helper
    private void scroll() {
        if (cursorY >= height) {
            insertEmptyLineAtBottom();
            cursorY = height - 1;
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

    // Content Access
    public String getScreenAsString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < screen.size(); i++) {
            sb.append(screen.get(i).getLineAsString());
            if (i < screen.size() - 1) {
                sb.append("\n");    // add newline character at the end of every (but not last) line
            }
        }
        return sb.toString();
    }

    public String getScreenAndHistoryAsString() {
        StringBuilder sb = new StringBuilder();
        for (TerminalLine line : scrollback) {
            sb.append(line.getLineAsString()).append('\n');
        }
        sb.append(getScreenAsString());
        return sb.toString();
    }

    private TerminalLine getLine(int y) {
        if (y >= 0 && y < height) {
            // Screen coordinates
            return screen.get(y);
        } else if (y < 0 && Math.abs(y) <= scrollback.size()) {
            // Scrollback
            // Conversion of deque to ArrayList (indexed access)
            List<TerminalLine> history = new ArrayList<>(scrollback);
            return history.get(history.size() + y); // y = -1, the newest line
        }
        return null;
    }

    public char getCharAtPos(int x, int y) {
        TerminalLine line = getLine(y);
        if (line != null && x >= 0 && x < width) {
            return line.getCell(x).ch(); // return char if position is valid
        }
        return ' '; // return empty (not found)
    }

    public CellAttribute getAttributeAtPos(int x, int y) {
        TerminalLine line = getLine(y);
        if (line != null && x >= 0 && x < width) {
            Cell cell = line.getCell(x);
            CellAttribute attribute = new CellAttribute();

            attribute.setBgColor(cell.bgColor());
            attribute.setFgColor(cell.fgColor());
            attribute.setStyle(cell.style());
            return attribute;
        }
        return new CellAttribute(); // return empty cell (not found)
    }

    public String getLineAsString(int y) {
        TerminalLine line = getLine(y);
        if (line != null) {
            return line.getLineAsString();
        }
        return "";
    }


}
