package me.lkp111138.deebot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;
import me.lkp111138.deebot.DeeBot;
import me.lkp111138.deebot.Main;
import me.lkp111138.deebot.Translation;
import me.lkp111138.deebot.game.Game;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JoinCommand implements BaseCommand {
    @Override
    public void respond(TelegramBot bot, Message msg, String[] args) {
        // joins a game
        long gid = msg.chat().id();
        Game g = Game.byGroup(gid);
        if (g == null) {
            bot.execute(new SendMessage(msg.chat().id(), Translation.get(DeeBot.lang(gid), "NO_GAME_TO_JOIN")).replyToMessageId(msg.messageId()));
            try (Connection conn = Main.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement("INSERT IGNORE INTO tg_users (tgid, username) VALUES (?, ?)");
                stmt.setInt(1, msg.from().id());
                stmt.setString(2, msg.from().username());
                stmt.execute();
            } catch (SQLException e) {
                e.printStackTrace();
                bot.execute(new SendMessage(msg.chat().id(), "An error occurred: " + e.getMessage()).replyToMessageId(msg.messageId()));
            }
        } else {
            g.addPlayer(msg);
        }
    }
}
