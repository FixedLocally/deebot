package me.lkp111138.deebot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;
import me.lkp111138.deebot.DeeBot;
import me.lkp111138.deebot.Main;
import me.lkp111138.deebot.translation.Translation;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class NextGameCommand implements Command {
    @Override
    public void respond(TelegramBot bot, Message msg, String[] args) {
        try (PreparedStatement stmt = Main.getConnection().prepareStatement("replace into next_game values (?, ?)")) {
            long gid = msg.chat().id();
            int uid = msg.from().id();
            stmt.setInt(1, uid);
            stmt.setLong(2, gid);
            stmt.execute();
            SendMessage send = new SendMessage(uid, Translation.get(DeeBot.lang(gid)).NEXT_GAME_QUEUED(msg.chat().title()));
            bot.execute(send);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
