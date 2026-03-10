package models;

import models.enums.Color;
import models.enums.Style;

public class CellAttribute {

    private Color fgColor = Color.DEFAULT;
    private Color bgColor = Color.DEFAULT;
    private int style     = Style.NONE;

    // Getters & Setters
    public Color getFgColor() { return fgColor; }
    public Color getBgColor() { return bgColor; }
    public int getStyle() { return style; }

    public void setFgColor(Color fgColor) { this.fgColor = fgColor; }
    public void setBgColor(Color bgColor) { this.bgColor = bgColor; }

    // Styles can be:
    // - set directly
    // - added to current ones
    // - removed given style
    // - reset style completely
    // using bit operations, memory optimization
    public void setStyle(int style) { this.style = style; }
    public void addStyle(int style) { this.style |= style;} // bitwise OR
    public void removeStyle(int style) { this.style &= ~style; } // bitwise AND with negation
    public void resetStyle() { this.style = Style.NONE; }
}
