package me.lkp111138.deebot.game.achievement;

import me.lkp111138.deebot.game.Game;

public class DeepFriedAchievement extends Achievement {
    @Override
    public boolean canUnlock(Game.GameResult result, PlayerProfile profile, int index) {
        int remaining = 0;
        for (int i = 0; i < 4; i++) {
            if (i == index) {
                continue;
            }
            remaining += result.getEndDecks()[i].length;
        }
        return remaining == 39;
    }

    @Override
    public String getTag() {
        return "DEEP_FRIED";
    }
}
