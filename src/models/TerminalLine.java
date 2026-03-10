package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Manages one row
// TODO: usecases, for now I can't really tell what this class needs
public class TerminalLine {

    // Row is a stable container for one line in terminal. For now, you can't dynamically change the size of terminal window,
    // so initialized list with exact size (width).
    private final List<Cell> row;
    private final int width;

    // Constructor
    public TerminalLine(int width) {
        this.width = width;
        this.row = new ArrayList<>(width);

        // Initialize with empty cells
        for (int i = 0; i < width; i++) {
            row.add(Cell.createEmpty());
        }
    }

    public void setCell(int x, Cell cell) {
        if (x >= 0 && x < width) {
            row.set(x, cell); // set cell on index x
        }
    }

    public Cell getCell(int x) {
        if (x >= 0 && x < width) {
            return row.get(x);
        }
        // for safety
        return Cell.createEmpty();
    }

    // Return unmodifiable list so it is blocked to change state of the row from the outside.
    public List<Cell> getRow() { return Collections.unmodifiableList(row); }
    public int getWidth() { return width; }

}
