package ru.simplecasino.blackjack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Ranks 0..12 (A,2..10,J,Q,K), suits ignored for scoring. */
public final class BlackjackCards {
    private BlackjackCards() {}

    public static int rank(int card) {
        return card % 13;
    }

    public static int hardValue(int card) {
        int r = rank(card);
        if (r == 0) return 11; // Ace
        if (r >= 10) return 10; // J Q K
        return r + 1; // 2..10 -> rank 1..9 maps to 2..10
    }

    /** Best score <= 21, or minimum if all bust. */
    public static int score(List<Integer> hand) {
        int total = 0;
        int aces = 0;
        for (int c : hand) {
            int r = rank(c);
            if (r == 0) {
                aces++;
                total += 11;
            } else if (r >= 10) {
                total += 10;
            } else {
                total += r + 1;
            }
        }
        while (total > 21 && aces > 0) {
            total -= 10;
            aces--;
        }
        return total;
    }

    public static boolean isBust(List<Integer> hand) {
        return score(hand) > 21;
    }

    public static List<Integer> freshShuffledDeck(Random random) {
        List<Integer> deck = new ArrayList<>(52);
        for (int i = 0; i < 52; i++) {
            deck.add(i);
        }
        Collections.shuffle(deck, random);
        return deck;
    }

    public static String cardName(int card) {
        String[] ranks = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        return ranks[rank(card)];
    }
}
