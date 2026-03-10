package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Manages one row
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


    public void fill(Cell cell) {
        for (int i = 0; i < width; i++ ) {
            row.set(i, cell);
        }
    }

    public Cell insertCell(int x, Cell cell) {
        if (x < 0 || x >= width) return Cell.createEmpty();

        Cell cellToCarry = row.get(width - 1); // cell that will be carried to next row

        // Set cell[i-1] to cell[i] sequentially
        for (int i = width - 1; i > x; i--) {
            row.set(i, row.get(i-1));
        }
        row.set(x, cell);
        return cellToCarry;
    }

    public String getLineAsString() {
        StringBuilder sb = new StringBuilder(width);
        for (Cell cell : row) {
            sb.append(cell.ch());
        }
        return sb.toString();
    }

}
