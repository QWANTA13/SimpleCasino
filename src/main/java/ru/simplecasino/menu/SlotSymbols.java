package ru.simplecasino.menu;

/**
 * Symbols on the reels. Index is stored in menu data slots.
 */
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

    /** Payout multiplier for three matching symbols (bet * multiplier). */
    public static int tripleMultiplier(int symbol) {
        return switch (symbol) {
            case CHERRY -> 4;
            case LEMON -> 6;
            case SEVEN -> 20;
            case DIAMOND -> 10;
            case BAR -> 40;
            default -> 0;
        };
    }
}
