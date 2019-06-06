package me.lkp111138.deebot.game.achievement;

import me.lkp111138.deebot.game.Game;

public class FamiliarizedAchievement extends Achievement {
    @Override
    public boolean canUnlock(Game.GameResult result, PlayerProfile profile, int index) {
        return profile.game_count >= 200;
    }

    @Override
    public String getTag() {
        return "FAMILIARIZED";
    }
}
