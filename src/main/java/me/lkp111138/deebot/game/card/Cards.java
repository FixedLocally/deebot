package me.lkp111138.deebot.game.card;

import java.util.Random;

public class Cards {
    public static Card[] shuffle() {
        Card[] cards = Card.values();
        Random rand = new Random();
        for (int i = 0; i < 52; ++i) {
            int idx = rand.nextInt(52);
            Card tmp = cards[i];
            cards[i] = cards[idx];
            cards[idx] = tmp;
        }
        return cards;
    }

    public enum Card implements CharSequence {
        D3,
        C3,
        H3,
        S3,
        D4,
        C4,
        H4,
        S4,
        D5,
        C5,
        H5,
        S5,
        D6,
        C6,
        H6,
        S6,
        D7,
        C7,
        H7,
        S7,
        D8,
        C8,
        H8,
        S8,
        D9,
        C9,
        H9,
        S9,
        D10,
        C10,
        H10,
        S10,
        DJ,
        CJ,
        HJ,
        SJ,
        DQ,
        CQ,
        HQ,
        SQ,
        DK,
        CK,
        HK,
        SK,
        DA,
        CA,
        HA,
        SA,
        D2,
        C2,
        H2,
        S2,
        ON99;

        @Override
        public int length() {
            return name().length();
        }

        @Override
        public char charAt(int i) {
            return name().charAt(i);
        }

        @Override
        public CharSequence subSequence(int i, int i1) {
            return name().subSequence(i, i1);
        }

        public Suit getSuit() {
            return Suit.values[ordinal() & 3];
        }

        public int getFace() {
            return ordinal() >> 2;
        }
    }

    public enum Suit {
        DIAMOND,
        CLUB,
        HEART,
        SPADE;

        private static Suit[] values = values();
    }
}
