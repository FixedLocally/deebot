package me.lkp111138.deebot.game.achievement;

import me.lkp111138.deebot.game.Game;

public class LoseItAllAchievement extends Achievement {
    @Override
    public boolean canUnlock(Game.GameResult result, PlayerProfile profile, int index) {
        return result.getEndDecks()[index].length == 13;
    }

    @Override
    public String getTag() {
        return "LOSE_IT_ALL";
    }
}
