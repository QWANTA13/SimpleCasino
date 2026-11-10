package ru.simplecasino.menu;

public final class SlotSymbols {
    private SlotSymbols() {}

    public static final int CHERRY = 0;
    public static final int LEMON = 1;
    public static final int SEVEN = 2;
    public static final int DIAMOND = 3;
    public static final int BAR = 4;
    public static final int COUNT = 5;

    public static final String[] NAMES = {
            "cherry", "lemon", "seven", "diamond", "bar"
    };

    public static int tripleMultiplier(int symbol) {
        return switch (symbol) {
            case CHERRY -> 4;
            case LEMON -> 6;
            case DIAMOND -> 10;
            case SEVEN -> 20;
            case BAR -> 40;
            default -> 0;
        };
    }
}
