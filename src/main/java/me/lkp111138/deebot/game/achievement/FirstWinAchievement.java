package me.lkp111138.deebot.game.achievement;

import me.lkp111138.deebot.game.Game;

public class FirstWinAchievement extends Achievement {
    @Override
    public boolean canUnlock(Game.GameResult result, PlayerProfile profile, int index) {
        return profile.won_count >= 1;
    }

    @Override
    public String getTag() {
        return "FIRST_WIN";
    }
}
