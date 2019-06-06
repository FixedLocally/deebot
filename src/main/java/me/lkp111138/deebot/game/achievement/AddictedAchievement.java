package me.lkp111138.deebot.game.achievement;

import me.lkp111138.deebot.game.Game;

public class AddictedAchievement extends Achievement {
    @Override
    public boolean canUnlock(Game.GameResult result, PlayerProfile profile, int index) {
        return profile.game_count >= 1000;
    }

    @Override
    public String getTag() {
        return "ADDICTED";
    }
}
