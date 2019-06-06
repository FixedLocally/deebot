package me.lkp111138.deebot.game.achievement;

import me.lkp111138.deebot.Main;
import me.lkp111138.deebot.game.Game;

public class PlayWithMintAchievement extends Achievement {
    @Override
    public boolean canUnlock(Game.GameResult result, PlayerProfile profile, int index) {
        int[] players = result.getPlayers();
        for (int i = 0; i < 4; i++) {
            if (players[i] == Main.BOT_OWNER) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getTag() {
        return "PLAY_WITH_MINT";
    }
}
