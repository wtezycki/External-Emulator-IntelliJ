package models;

import models.enums.Color;
import models.enums.Style;

// Cell is immutable record.
public record Cell(
        char sign,
        Color bgColor,
        Color fgColor,
        int style           // int : bit sum of styles
) {
    // Create new, empty cell
    public static Cell createEmpty() {
        return new Cell(' ',
                Color.DEFAULT,
                Color.DEFAULT,
                0);
    }
}
