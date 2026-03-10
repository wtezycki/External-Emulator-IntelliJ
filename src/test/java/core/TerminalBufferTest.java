package test.java.core;

import core.TerminalBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TerminalBufferTest {

    private TerminalBuffer buffer;

    @BeforeEach
    void setUp() {
        buffer = new TerminalBuffer(5, 3, 5);
    }

    @Test
    void shouldClampCursorToBoundaries() {
        // Given a cursor position out of bounds
        buffer.setCursorPosition(-10, 500);

        // When writing a single character
        buffer.write("X");

        // Then the cursor should clamp to the bottom-left edge (0, 2)
        assertEquals('X', buffer.getCharAtPos(0, 2), "Cursor failed to clamp to the maximum valid Y boundary");
        assertEquals(' ', buffer.getCharAtPos(0, 0), "Character was written at an incorrect position (0, 0)");
    }

    @Test
    void shouldWrapTextCorrectly() {
        // Given a string whose length exceeds the terminal width
        String text = "1234567";

        // When writing the string to the buffer
        buffer.write(text);

        // Then the excess characters should wrap to the next line
        assertEquals("12345", buffer.getLineAsString(0), "The first line did not match the expected width limit.");
        assertEquals("67   ", buffer.getLineAsString(1), "The wrapped characters did not appear correctly on the second line.");
    }

    @Test
    void shouldScrollAndSaveToScrollback() {
        // Given 3 lines of text filling the screen
        buffer.write("L1\n");
        buffer.write("L2\n");
        buffer.write("L3\n");

        // When writing a 4th line to trigger the scrolling mechanism
        buffer.write("L4");

        // Then the screen should shift lines upwards
        assertEquals("L2   ", buffer.getLineAsString(0), "Screen line 0 should contain the shifted second line.");
        assertEquals("L3   ", buffer.getLineAsString(1), "Screen line 1 should contain the shifted third line.");
        assertEquals("L4   ", buffer.getLineAsString(2), "Screen line 2 should contain the newly written line.");

        // And the oldest line should be pushed to the scrollback history (y = -1)
        assertEquals("L1   ", buffer.getLineAsString(-1), "The scrolled out line was not found in the scrollback history.");
    }

    @Test
    void shouldInsertTextAndShiftExistingCharacters() {
        // Given existing text on the screen and cursor reset to origin
        buffer.write("Cat");
        buffer.setCursorPosition(0, 0);

        // When inserting a new string
        buffer.insert("My ");

        // Then the old characters should shift to the right
        assertEquals("My Ca", buffer.getLineAsString(0), "The inserted string did not shift the existing characters properly.");
        assertEquals("t    ", buffer.getLineAsString(1), "The carried character from the insertion did not carry to the next line.");
    }

    @Test
    void shouldClearScreenButKeepScrollback() {
        // Given a terminal with enough lines to trigger scrollback
        buffer.write("A\nB\nC\nD");

        // When clearing only the visible screen
        buffer.clearScreen();

        // Then the screen should be filled with empty cells
        assertEquals("     ", buffer.getLineAsString(0), "Screen was not properly cleared.");

        // And the scrollback should retain its historical data
        assertEquals("A    ", buffer.getLineAsString(-1), "Scrollback history was unexpectedly cleared.");
    }

    @Test
    void shouldClearScreenAndScrollback() {
        // Given a terminal with enough lines to trigger scrollback
        buffer.write("A\nB\nC\nD");

        // When clearing both the screen and the scrollback
        buffer.clearScreenAndScrollback();

        // Then the screen should be empty
        assertEquals("     ", buffer.getLineAsString(0), "Screen was not properly cleared.");

        // And the scrollback should return an empty string
        assertEquals("", buffer.getLineAsString(-1), "Scrollback history should be empty after clearScreenAndScrollback().");
    }

    @Test
    void shouldDropOldestLineWhenScrollbackIsFull() {
        // Given a terminal with a scrollback limit of 5 lines
        // When we write 10 lines (which is > height + maxScrollback)
        for (int i = 1; i <= 10; i++) {
            buffer.write("L" + i + "\n");
        }

        // Then the screen (height = 3) should show lines 9, 10, and an empty line
        assertEquals("L9", buffer.getLineAsString(0).trim(), "Screen line 0 should be 'L9'.");
        assertEquals("L10", buffer.getLineAsString(1).trim(), "Screen line 1 should be 'L10'.");

        // The scrollback has a capacity of 5.
        // It should contain lines 4, 5, 6, 7, 8. Lines 1, 2, 3 should be permanently lost.
        // y = -1 is L8, y = -5 is L4
        assertEquals("L8", buffer.getLineAsString(-1).trim(), "Newest scrollback line should be 'L8'.");
        assertEquals("L4", buffer.getLineAsString(-5).trim(), "Oldest scrollback line should be 'L4'.");

        // Attempting to access Line 3 (y = -6) should return an empty string
        assertEquals("", buffer.getLineAsString(-6), "Lines beyond maxScrollback should be deleted.");
    }

    @Test
    void shouldMoveCursorRelativelyAndClamp() {
        // Given cursor at default position (0,0)

        // When moving cursor right by 2 and down by 1
        buffer.moveCursor(2, 1);
        buffer.write("A");

        // Then 'A' should be at (2, 1)
        assertEquals('A', buffer.getCharAtPos(2, 1), "Cursor did not move to the correct relative position.");

        // When attempting to move cursor completely off the screen via relative move
        buffer.moveCursor(-10, 50);

        // Then cursor should clamp to bottom-left (0, 2)
        buffer.write("B");
        assertEquals('B', buffer.getCharAtPos(0, 2), "Relative cursor movement did not clamp to boundaries.");
    }

    @Test
    void shouldFillCurrentLineWithCharacter() {
        // Given a terminal
        buffer.setCursorPosition(0, 1); // Move to the middle line

        // When filling the line with '*'
        buffer.fillLine('*');

        // Then the entire line 1 should be filled with '*', and others should remain empty
        assertEquals("*****", buffer.getLineAsString(1), "The line was not completely filled.");
        assertEquals("     ", buffer.getLineAsString(0), "Other lines should not be affected.");
        assertEquals("     ", buffer.getLineAsString(2), "Other lines should not be affected.");
    }

    @Test
    void shouldHandleCarriageReturnWithoutLineFeed() {
        // Given text written on a line (4 chars, so it doesn't wrap yet)
        buffer.write("Word");

        // When a carriage return (\r) is sent, followed by new text
        buffer.write("\r");
        buffer.write("Bye");

        // Then the new text should overwrite the beginning of the line
        // 'Bye' overwrites 'Wor', leaving 'd' -> 'Byed '
        assertEquals("Byed ", buffer.getLineAsString(0), "Carriage return did not properly overwrite existing text.");
    }

    @Test
    void shouldApplyCurrentAttributesToNewCells() {
        // Given specific attributes set in the buffer
        buffer.setCurrentAttributes(models.enums.Color.RED, models.enums.Color.BLUE, models.enums.Style.BOLD);

        // When writing text
        buffer.write("X");

        // Then the cell should inherit these attributes
        models.CellAttribute attr = buffer.getAttributeAtPos(0, 0);
        assertEquals(models.enums.Color.RED, attr.getFgColor(), "Foreground color mismatch.");
        assertEquals(models.enums.Color.BLUE, attr.getBgColor(), "Background color mismatch.");
        assertEquals(models.enums.Style.BOLD, attr.getStyle(), "Style mismatch.");
    }

    @Test
    void shouldReturnEmptyCharWhenReadingOutOfBounds() {
        // Given an empty terminal

        // When asking for a character far beyond the screen
        char outOfBoundsChar = buffer.getCharAtPos(100, 100);

        // When asking for a character way too far in the scrollback history
        char tooOldHistoryChar = buffer.getCharAtPos(0, -50);

        // Then the buffer should safely return a space ' ' instead of crashing
        assertEquals(' ', outOfBoundsChar, "Should return space for out of bounds X/Y.");
        assertEquals(' ', tooOldHistoryChar, "Should return space for non-existent history.");
    }

    @Test
    void shouldInsertEmptyLineAtBottomAndPushToScrollback() {
        // Given a full screen
        buffer.write("A\nB\nC");

        // When manually inserting an empty line at the bottom
        buffer.insertEmptyLineAtBottom();

        // Then lines should shift up, 'A' goes to history, and bottom is empty
        assertEquals("B    ", buffer.getLineAsString(0), "Line 'B' should have shifted up.");
        assertEquals("C    ", buffer.getLineAsString(1), "Line 'C' should have shifted up.");
        assertEquals("     ", buffer.getLineAsString(2), "The bottom line should be empty.");
        assertEquals("A    ", buffer.getLineAsString(-1), "Line 'A' should be in the scrollback.");
    }


    @Test
    void shouldHandleEmptyStringsGracefully() {
        // Given an empty terminal

        // When writing and inserting empty strings
        buffer.write("");
        buffer.insert("");

        // Then nothing should change, cursor stays at (0,0) and screen is empty
        assertEquals("     ", buffer.getLineAsString(0), "Writing an empty string should not alter the screen.");
        assertEquals(' ', buffer.getCharAtPos(0, 0), "First cell should remain empty.");
    }

}