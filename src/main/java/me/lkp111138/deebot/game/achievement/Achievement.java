package me.lkp111138.deebot.game.achievement;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import me.lkp111138.deebot.Main;
import me.lkp111138.deebot.game.Game;
import me.lkp111138.deebot.translation.Translation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

public abstract class Achievement {
    // FIRST_GAME - play a game
    // FIRST_WIN - win a game
    // PLAY_WITH_MINT - play a game with the owner
    // FIRST_BLOOD - lose a game and lose chips along
    // ROOKIE - play 50 games
    // FAMILIARIZED - play 200 games
    // ADDICTED - play 1000 games
    // AMATEUR - win 20 games
    // ADEPT - win 100 games
    // EXPERT - win 500 games
    // LOSE_IT_ALL - lose a game with 13 cards left
    // DEEP_FRIED - win a game with all opponents played no cards

    private static HashSet<Achievement> achvs = new HashSet<>();

    public static void registerAchievement(Achievement achv) {
        achvs.add(achv);
    }

    public static void executeAchievements(TelegramBot bot, Game.GameResult result, int uid, int index) {
        HashSet<String> unlocked = new HashSet<>();
        try (PreparedStatement stmt = Main.getConnection().prepareStatement("SELECT achv FROM achv_log WHERE tgid=?")) {
            stmt.setInt(1, uid);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                unlocked.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        PlayerProfile profile;
        try (PreparedStatement stmt = Main.getConnection().prepareStatement("SELECT game_count, won_count, won_cards, lost_cards, chips FROM tg_users where tgid=?")) {
            stmt.setInt(1, uid);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            profile = new PlayerProfile(uid, rs.getInt(1), rs.getInt(2), rs.getInt(3),
                    rs.getInt(4), rs.getInt(5));
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(uid);
            return;
        }

        for (Achievement achv : achvs) {
            if (!unlocked.contains(achv.getTag()) && achv.canUnlock(result, profile, index)) {
                try (PreparedStatement stmt = Main.getConnection().prepareStatement("INSERT INTO achv_log (tgid, achv) VALUE (?, ?)")) {
                    stmt.setInt(1, uid);
                    stmt.setString(2, achv.getTag());
                    stmt.execute();
                    Translation translation = Translation.get(null);
                    SendMessage send = new SendMessage(uid, translation.ACHIEVEMENT_UNLOCKED() + translation.ACHIEVEMENT_PM(achv.getTag()));
                    send.parseMode(ParseMode.Markdown);
                    bot.execute(send);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public abstract boolean canUnlock(Game.GameResult result, PlayerProfile profile, int index);

    public abstract String getTag();

    public static class PlayerProfile {
        final int uid;
        final int game_count;
        final int won_count;
        final int won_cards;
        final int lost_cards;
        final int chips;

        private PlayerProfile(int uid, int game_count, int won_count, int won_cards, int lost_cards, int chips) {
            this.uid = uid;
            this.game_count = game_count;
            this.won_count = won_count;
            this.won_cards = won_cards;
            this.lost_cards = lost_cards;
            this.chips = chips;
        }
    }
}
