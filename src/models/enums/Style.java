package models.enums;

public class Style {
    // 000 -> NONE
    // 001 -> BOLD
    // 010 -> ITALIC
    // 011 -> BOLD | ITALIC
    // 100 -> UNDERLINE
    // 101 -> UNDERLINE | BOLD
    // 110 -> UNDERLINE | ITALIC
    // 111 -> UNDERLINE | ITALIC | BOLD

    // Used bit operations to optimize memory.
    // Other implementation could be by array of 8 states, but it would be less memory-efficient.
    public static final int NONE = 0;
    public static final int BOLD = 1;
    public static final int ITALIC = 2;
    public static final int UNDERLINE = 4;
}