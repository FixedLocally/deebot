package me.lkp111138.deebot.game.achievement;

import me.lkp111138.deebot.game.Game;

public class FirstBloodAchievement extends Achievement {
    @Override
    public boolean canUnlock(Game.GameResult result, PlayerProfile profile, int index) {
        return result.getOffsets()[index] < 0;
    }

    @Override
    public String getTag() {
        return "FIRST_BLOOD";
    }
}
