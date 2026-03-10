package models;

import models.enums.Color;
import models.enums.Style;

public record Cell() {
    static char sign;
    static Color bgColor;
    static Color fgColor;
    static Style style;
}
