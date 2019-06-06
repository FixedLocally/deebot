package me.lkp111138.deebot.game.achievement;

import me.lkp111138.deebot.game.Game;

public class RookieAchievement extends Achievement {
    @Override
    public boolean canUnlock(Game.GameResult result, PlayerProfile profile, int index) {
        return profile.game_count >= 50;
    }

    @Override
    public String getTag() {
        return "ROOKIE";
    }
}
