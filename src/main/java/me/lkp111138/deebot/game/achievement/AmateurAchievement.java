package me.lkp111138.deebot.game.achievement;

import me.lkp111138.deebot.game.Game;

public class AmateurAchievement extends Achievement {
    @Override
    public boolean canUnlock(Game.GameResult result, PlayerProfile profile, int index) {
        return profile.won_count >= 20;
    }

    @Override
    public String getTag() {
        return "AMATEUR";
    }
}
