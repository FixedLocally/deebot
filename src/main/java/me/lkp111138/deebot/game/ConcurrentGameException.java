package me.lkp111138.deebot.game;

public class ConcurrentGameException extends Exception {
    private final Game game;

    ConcurrentGameException(Game g) {
        game = g;
    }

    public Game getGame() {
        return game;
    }
}
