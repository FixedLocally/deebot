package me.lkp111138.deebot.game.achievement;

import me.lkp111138.deebot.game.Game;

public class FirstGameAchievement extends Achievement {
    @Override
    public boolean canUnlock(Game.GameResult result, PlayerProfile uid, int index) {
        return true;
    }

    @Override
    public String getTag() {
        return "FIRST_GAME";
    }
}
